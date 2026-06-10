export const env = {
  apiGatewayUrl: import.meta.env.VITE_API_BASE_URL ?? import.meta.env.VITE_API_GATEWAY_URL ?? 'http://localhost:8080',
  appName: import.meta.env.VITE_APP_NAME ?? 'Care Health',
  appVersion: import.meta.env.VITE_APP_VERSION ?? '1.0.0',
  environment: import.meta.env.MODE,
};
