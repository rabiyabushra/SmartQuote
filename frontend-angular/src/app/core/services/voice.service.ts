import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { VoiceFieldName, VoiceTranscriptResponse } from '../models/voice.model';
import { ParsedQuotationResponse } from '../models/parsed-quotation.model';

export class VoiceServiceError extends Error {
  constructor(
    message: string,
    readonly code:
      | 'unsupported'
      | 'permission_denied'
      | 'recording_failed'
      | 'empty_recording'
      | 'transcription_failed'
      | 'network'
      | 'unknown' = 'unknown'
  ) {
    super(message);
    this.name = 'VoiceServiceError';
  }
}

@Injectable({
  providedIn: 'root'
})
export class VoiceService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  private mediaRecorder: MediaRecorder | null = null;
  private mediaStream: MediaStream | null = null;
  private chunks: Blob[] = [];
  private recordingPromise: Promise<void> | null = null;

  isRecordingSupported(): boolean {
    return typeof navigator !== 'undefined'
      && !!navigator.mediaDevices?.getUserMedia
      && typeof MediaRecorder !== 'undefined';
  }

  isSpeechRecognitionSupported(): boolean {
    return typeof window !== 'undefined'
      && (('SpeechRecognition' in window) || ('webkitSpeechRecognition' in window));
  }

  listenToSpeech(): Observable<string> {
    return new Observable<string>((observer) => {
      const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      if (!SpeechRecognition) {
        observer.error(new VoiceServiceError('Speech recognition not supported in this browser.', 'unsupported'));
        return;
      }

      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = 'en-US';

      recognition.onresult = (event: any) => {
        const transcript = event?.results?.[0]?.[0]?.transcript ?? '';
        observer.next(transcript);
        observer.complete();
      };

      recognition.onerror = (event: any) => {
        observer.error(new VoiceServiceError('Speech recognition failed. Please try again or enter text manually.', 'transcription_failed'));
      };

      recognition.onend = () => {
        observer.complete();
      };

      try {
        recognition.start();
      } catch (e) {
        observer.error(new VoiceServiceError('Unable to start microphone listening.', 'recording_failed'));
      }

      return () => {
        try { recognition.stop(); } catch (e) {}
      };
    });
  }

  async startRecording(): Promise<void> {
    if (!this.isRecordingSupported()) {
      throw new VoiceServiceError(
        'Voice recording is not supported in this browser. Please use Chrome or Edge.',
        'unsupported'
      );
    }

    if ((this.mediaRecorder?.state as string) === 'recording') {
      return;
    }

    if (this.recordingPromise) {
      await this.recordingPromise;
      if ((this.mediaRecorder?.state as string) === 'recording') {
        return;
      }
    }

    this.recordingPromise = this.beginRecording();
    try {
      await this.recordingPromise;
    } finally {
      this.recordingPromise = null;
    }
  }

  async stopRecording(): Promise<Blob> {
    if (!this.mediaRecorder) {
      throw new VoiceServiceError('No active recording found.', 'recording_failed');
    }

    if (this.mediaRecorder.state === 'inactive') {
      const blob = this.buildBlob();
      this.cleanupRecordingResources();
      if (blob.size === 0) {
        throw new VoiceServiceError('Recording was empty. Please try again.', 'empty_recording');
      }
      return blob;
    }

    return new Promise<Blob>((resolve, reject) => {
      const recorder = this.mediaRecorder;
      if (!recorder) {
        reject(new VoiceServiceError('No active recording found.', 'recording_failed'));
        return;
      }

      recorder.onstop = () => {
        const blob = this.buildBlob();
        this.cleanupRecordingResources();
        if (blob.size === 0) {
          reject(new VoiceServiceError('Recording was empty. Please try again.', 'empty_recording'));
          return;
        }
        resolve(blob);
      };

      recorder.onerror = () => {
        this.cleanupRecordingResources();
        reject(new VoiceServiceError('Recording failed. Please try again.', 'recording_failed'));
      };

      recorder.stop();
    });
  }

  transcribeAudio(
    audio: Blob,
    quotationId: string,
    fieldName: VoiceFieldName
  ): Observable<VoiceTranscriptResponse> {
    const formData = new FormData();
    formData.append('quotationId', quotationId);
    formData.append('fieldName', fieldName);
    formData.append('audio', audio, this.resolveFileName(audio.type));

    return this.http
      .post<VoiceTranscriptResponse>(this.api.url('/voice/transcribe'), formData)
      .pipe(
        map((response) => ({
          ...response,
          transcript: (response.transcript ?? '').trim()
        })),
        catchError((error: HttpErrorResponse) =>
          throwError(() => this.toVoiceServiceError(error))
        )
      );
  }

  parseTranscript(transcript: string): Observable<ParsedQuotationResponse> {
    return this.http
      .post<ParsedQuotationResponse>(this.api.url('/quotations/parse'), { transcript })
      .pipe(
        catchError((error: HttpErrorResponse) =>
          throwError(() => this.toVoiceServiceError(error))
        )
      );
  }

  cancelRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.onstop = null;
      this.mediaRecorder.stop();
    }
    this.cleanupRecordingResources();
  }

  private async beginRecording(): Promise<void> {
    this.cleanupRecordingResources();
    this.chunks = [];

    try {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch (error) {
      const domError = error as DOMException;
      if (domError.name === 'NotAllowedError' || domError.name === 'PermissionDeniedError') {
        throw new VoiceServiceError(
          'Microphone permission was denied. Please allow microphone access and try again.',
          'permission_denied'
        );
      }
      if (domError.name === 'NotFoundError' || domError.name === 'DevicesNotFoundError') {
        throw new VoiceServiceError(
          'No microphone was found. Please connect a microphone and try again.',
          'recording_failed'
        );
      }
      throw new VoiceServiceError(
        'Unable to access the microphone. Please check your device settings.',
        'recording_failed'
      );
    }

    const mimeType = this.pickMimeType();
    this.mediaRecorder = mimeType
      ? new MediaRecorder(this.mediaStream, { mimeType })
      : new MediaRecorder(this.mediaStream);

    this.mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        this.chunks.push(event.data);
      }
    };

    this.mediaRecorder.start();
  }

  private buildBlob(): Blob {
    const type = this.mediaRecorder?.mimeType || this.chunks[0]?.type || 'audio/webm';
    return new Blob(this.chunks, { type });
  }

  private cleanupRecordingResources(): void {
    this.mediaRecorder = null;
    this.chunks = [];

    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach((track) => track.stop());
      this.mediaStream = null;
    }
  }

  private pickMimeType(): string | undefined {
    const candidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus', 'audio/mp4'];
    return candidates.find((candidate) => MediaRecorder.isTypeSupported(candidate));
  }

  private resolveFileName(mimeType: string): string {
    if (mimeType.includes('ogg')) {
      return 'recording.ogg';
    }
    if (mimeType.includes('mp4')) {
      return 'recording.m4a';
    }
    return 'recording.webm';
  }

  private toVoiceServiceError(error: HttpErrorResponse): VoiceServiceError {
    const message = typeof error.error?.message === 'string'
      ? error.error.message
      : 'Unable to process request. Please try again or enter text manually.';

    if (error.status === 0) {
      return new VoiceServiceError(
        'Backend server is offline (http://localhost:8080). Please start the Spring Boot backend.',
        'network'
      );
    }

    return new VoiceServiceError(message, 'transcription_failed');
  }
}
