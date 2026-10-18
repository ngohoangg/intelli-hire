export const environment = {
  production: false,
  apiUrl: '/api',
  // TEMPORARY: fake CV parsing in the UI until resume-service parsing is fixed (see core/mocks)
  mockCvParse: true,
  // TEMPORARY: fake interview create/room/grading/report in the UI (see core/mocks)
  mockInterview: true
};
