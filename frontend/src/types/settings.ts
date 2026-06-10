export type SettingsCategory = 'GENERAL' | 'MEDICAL' | 'INSURANCE' | 'SECURITY';
export type DensityMode = 'comfortable' | 'compact';
export type AnimationMode = 'normal' | 'reduced';

export interface SettingResponse {
  id: string;
  category: SettingsCategory | string;
  key: string;
  value: string;
  description?: string;
  active: boolean;
}

export interface SettingsCategoryResponse {
  category: SettingsCategory | string;
  settings: SettingResponse[];
}

export interface UpsertSettingPayload {
  key: string;
  value: string;
  description?: string;
  active: boolean;
}

export interface UiPreferences {
  density: DensityMode;
  animations: AnimationMode;
}
