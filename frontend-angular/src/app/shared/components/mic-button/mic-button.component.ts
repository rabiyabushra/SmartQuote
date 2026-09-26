import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription, firstValueFrom } from 'rxjs';
import { VoiceService, VoiceServiceError } from '../../../core/services/voice.service';
import { MicButtonState, VoiceFieldName } from '../../../core/models/voice.model';
import { normalizeSpokenNumber } from '../../utils/voice-number.util';

export type MicFieldType = 'text' | 'number';

@Component({
  selector: 'app-mic-button',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './mic-button.component.html',
  styleUrl: './mic-button.component.scss'
})
export class MicButtonComponent {
  private readonly voiceService = inject(VoiceService);

  @Input({ required: true }) quotationId!: string;
  @Input({ required: true }) fieldName!: VoiceFieldName;
  @Input() fieldType: MicFieldType = 'text';
  @Input() disabled = false;
  @Input() ariaLabel = 'Record voice input';

  @Output() recognized = new EventEmitter<string>();
  @Output() voiceError = new EventEmitter<string>();

  state: MicButtonState = 'idle';
  statusMessage = '';

  private autoStopTimeout: any = null;
  private speechSubscription: Subscription | null = null;

  get isBusy(): boolean {
    return this.state === 'recording' || this.state === 'processing';
  }

  async onMicClick(): Promise<void> {
    if (this.disabled || this.state === 'processing') {
      return;
    }

    if (this.state === 'recording') {
      await this.finishRecording();
      return;
    }

    if (this.state === 'idle' || this.state === 'success' || this.state === 'error') {
      await this.startRecording();
    }
  }

  private async startRecording(): Promise<void> {
    this.voiceError.emit('');
    this.statusMessage = 'Listening...';
    this.state = 'recording';

    // Prefer Web Speech API for instant native browser recognition if supported
    if (this.voiceService.isSpeechRecognitionSupported()) {
      this.speechSubscription = this.voiceService.listenToSpeech().subscribe({
        next: (transcript) => {
          this.processRecognizedText(transcript);
        },
        error: (err) => {
          // Fallback to MediaRecorder + backend transcription if Web Speech API errors out
          this.fallbackToMediaRecorder();
        }
      });

      // Auto-stop after 5 seconds of listening
      this.autoStopTimeout = setTimeout(() => {
        if (this.state === 'recording') {
          this.speechSubscription?.unsubscribe();
          this.resetToIdle();
        }
      }, 5000);
      return;
    }

    await this.fallbackToMediaRecorder();
  }

  private async fallbackToMediaRecorder(): Promise<void> {
    try {
      await this.voiceService.startRecording();
      this.state = 'recording';
      this.statusMessage = 'Recording...';

      // Auto-finish after 4 seconds of recording
      this.autoStopTimeout = setTimeout(() => {
        if (this.state === 'recording') {
          this.finishRecording();
        }
      }, 4000);
    } catch (error) {
      this.handleError(error);
    }
  }

  private async finishRecording(): Promise<void> {
    if (this.autoStopTimeout) {
      clearTimeout(this.autoStopTimeout);
      this.autoStopTimeout = null;
    }

    if (this.speechSubscription) {
      this.speechSubscription.unsubscribe();
      this.speechSubscription = null;
    }

    this.state = 'processing';
    this.statusMessage = 'Processing...';

    try {
      const audio = await this.voiceService.stopRecording();
      const response = await firstValueFrom(
        this.voiceService.transcribeAudio(audio, this.quotationId, this.fieldName)
      );

      const transcript = response.transcript?.trim() ?? '';
      this.processRecognizedText(transcript);
    } catch (error) {
      this.handleError(error);
    }
  }

  private processRecognizedText(transcript: string): void {
    if (this.autoStopTimeout) {
      clearTimeout(this.autoStopTimeout);
      this.autoStopTimeout = null;
    }

    const trimmed = transcript.trim();
    if (!trimmed) {
      this.handleError(new VoiceServiceError('No speech was detected. Please speak clearly and try again.', 'transcription_failed'));
      return;
    }

    if (this.fieldType === 'number') {
      const normalized = normalizeSpokenNumber(trimmed);
      if (normalized.value === null) {
        this.handleError(new VoiceServiceError(normalized.error ?? 'Could not confidently recognize a numeric value.', 'transcription_failed'));
        return;
      }
      this.recognized.emit(String(normalized.value));
    } else {
      this.recognized.emit(trimmed);
    }

    this.state = 'success';
    this.statusMessage = 'Recognized';
    setTimeout(() => {
      if (this.state === 'success') {
        this.resetToIdle();
      }
    }, 1800);
  }

  private handleError(error: unknown): void {
    if (this.autoStopTimeout) {
      clearTimeout(this.autoStopTimeout);
      this.autoStopTimeout = null;
    }

    const message = error instanceof VoiceServiceError
      ? error.message
      : error instanceof Error
        ? error.message
        : 'Voice input failed. Please try again or enter the value manually.';

    this.state = 'error';
    this.statusMessage = message;
    this.voiceError.emit(message);
    this.voiceService.cancelRecording();
  }

  private resetToIdle(): void {
    if (this.autoStopTimeout) {
      clearTimeout(this.autoStopTimeout);
      this.autoStopTimeout = null;
    }
    this.state = 'idle';
    this.statusMessage = '';
  }
}
