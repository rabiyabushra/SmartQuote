export type VoiceFieldName = 'itemDescription' | 'quantity' | 'rate';

export interface VoiceTranscriptResponse {
  id?: string;
  quotationId?: string;
  fieldName?: VoiceFieldName;
  transcript: string;
  confidenceScore?: number;
  audioUrl?: string;
}

export type MicButtonState = 'idle' | 'recording' | 'processing' | 'success' | 'error';
