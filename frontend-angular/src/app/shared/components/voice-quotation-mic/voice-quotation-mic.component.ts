import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Subscription, firstValueFrom } from 'rxjs';
import { VoiceService, VoiceServiceError } from '../../../core/services/voice.service';
import { ParsedQuotationResponse } from '../../../core/models/parsed-quotation.model';

@Component({
  selector: 'app-voice-quotation-mic',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatFormFieldModule
  ],
  templateUrl: './voice-quotation-mic.component.html',
  styleUrl: './voice-quotation-mic.component.scss'
})
export class VoiceQuotationMicComponent {
  private readonly voiceService = inject(VoiceService);

  @Output() quotationParsed = new EventEmitter<ParsedQuotationResponse>();
  @Output() parseError = new EventEmitter<string>();

  state = signal<'idle' | 'recording' | 'processing' | 'success' | 'error'>('idle');
  statusMessage = signal<string>('');
  transcriptText = signal<string>('');
  recordingDurationSeconds = signal<number>(0);

  private timerInterval: any = null;
  private speechSubscription: Subscription | null = null;
  private maxRecordingTimeout: any = null;

  get isBusy(): boolean {
    return this.state() === 'recording' || this.state() === 'processing';
  }

  toggleRecording(): void {
    if (this.state() === 'processing') return;

    if (this.state() === 'recording') {
      this.stopRecordingAndParse();
    } else {
      this.startRecording();
    }
  }

  async startRecording(): Promise<void> {
    this.parseError.emit('');
    this.statusMessage.set('Listening to your quotation...');
    this.state.set('recording');
    this.startTimer();

    if (this.voiceService.isSpeechRecognitionSupported()) {
      this.speechSubscription = this.voiceService.listenToSpeech().subscribe({
        next: (transcript) => {
          if (transcript) {
            this.transcriptText.set(transcript);
          }
          this.stopRecordingAndParse();
        },
        error: () => {
          this.fallbackToMediaRecorder();
        }
      });

      this.maxRecordingTimeout = setTimeout(() => {
        if (this.state() === 'recording') {
          this.stopRecordingAndParse();
        }
      }, 25000);
      return;
    }

    await this.fallbackToMediaRecorder();
  }

  private async fallbackToMediaRecorder(): Promise<void> {
    try {
      await this.voiceService.startRecording();
      this.state.set('recording');
      this.statusMessage.set('Recording audio quotation...');

      this.maxRecordingTimeout = setTimeout(() => {
        if (this.state() === 'recording') {
          this.stopRecordingAndParse();
        }
      }, 25000);
    } catch (err) {
      this.handleError(err);
    }
  }

  async stopRecordingAndParse(): Promise<void> {
    this.stopTimer();

    if (this.maxRecordingTimeout) {
      clearTimeout(this.maxRecordingTimeout);
      this.maxRecordingTimeout = null;
    }

    if (this.speechSubscription) {
      this.speechSubscription.unsubscribe();
      this.speechSubscription = null;
    }

    const currentText = this.transcriptText().trim();

    if (currentText) {
      await this.parseTranscript(currentText);
      return;
    }

    this.state.set('processing');
    this.statusMessage.set('Transcribing voice audio...');

    try {
      const audioBlob = await this.voiceService.stopRecording();
      const sttResponse = await firstValueFrom(
        this.voiceService.transcribeAudio(audioBlob, 'NEW_VOICE_QUOTATION', 'itemDescription')
      );
      const recognized = sttResponse.transcript?.trim() ?? '';

      if (!recognized) {
        throw new VoiceServiceError('No speech was detected. Please try speaking again.', 'transcription_failed');
      }

      this.transcriptText.set(recognized);
      await this.parseTranscript(recognized);
    } catch (error) {
      this.handleError(error);
    }
  }

  async parseManualText(): Promise<void> {
    const text = this.transcriptText().trim();
    if (!text) {
      this.parseError.emit('Please speak or type a quotation transcript first.');
      return;
    }
    await this.parseTranscript(text);
  }

  private async parseTranscript(transcript: string): Promise<void> {
    this.state.set('processing');
    this.statusMessage.set('Analyzing quotation with local parser...');

    try {
      const parsed = await firstValueFrom(this.voiceService.parseTranscript(transcript));
      this.state.set('success');
      this.statusMessage.set('Quotation parsed successfully!');
      this.quotationParsed.emit(parsed);

      setTimeout(() => {
        if (this.state() === 'success') {
          this.state.set('idle');
          this.statusMessage.set('');
        }
      }, 2000);
    } catch (error) {
      this.handleError(error);
    }
  }

  private handleError(error: unknown): void {
    this.stopTimer();
    this.voiceService.cancelRecording();

    const msg = error instanceof VoiceServiceError
      ? error.message
      : error instanceof Error
        ? error.message
        : 'Failed to process voice quotation. Please try again.';

    this.state.set('error');
    this.statusMessage.set(msg);
    this.parseError.emit(msg);
  }

  private startTimer(): void {
    this.stopTimer();
    this.recordingDurationSeconds.set(0);
    this.timerInterval = setInterval(() => {
      this.recordingDurationSeconds.update(s => s + 1);
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  loadExample(exampleText: string): void {
    this.transcriptText.set(exampleText);
    this.parseManualText();
  }
}
