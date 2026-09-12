import api from './api';

export const getDashboardMetrics = async () => {
    const response = await api.get('/dashboard/metrics');
    return response.data;
};

export const getDashboardTraces = async () => {
    const response = await api.get('/dashboard/traces');
    return response.data;
};

export const getTraceDetails = async (requestId) => {
    const response = await api.get(`/dashboard/traces/${requestId}`);
    return response.data;
};

export const getDashboardApprovals = async () => {
    const response = await api.get('/dashboard/approvals');
    return response.data;
};

export const getEvaluations = async () => {
    const response = await api.get('/evaluations');
    return response.data;
};

export const evaluateTrace = async (traceId) => {
    const response = await api.post(`/evaluations/trace/${traceId}`);
    return response.data;
};
