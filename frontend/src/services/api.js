import axios from 'axios';
import { getCookie } from '../utils/cookies';

const API_URL = 'http://localhost:8080/api';

const apiClient = axios.create({
    baseURL: API_URL
});

apiClient.interceptors.request.use((config) => {
    const token = getCookie('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export const uploadDocument = async (file, documentType, uploadedByUserId = 1) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    formData.append('uploadedByUserId', uploadedByUserId);

    const response = await apiClient.post(`/documents/upload`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

export const getDocuments = async () => {
    const response = await apiClient.get(`/documents/`);
    return response.data;
};

export const generateComplianceReport = async (data) => {
    const response = await apiClient.post(`/compliance-report`, data);
    return response.data;
};

export const getAllUsers = async () => {
    const response = await apiClient.get(`/users/`);
    return response.data;
};

export const login = async (username, password) => {
    // We can use standard axios here since auth doesn't need a token, 
    // or just use apiClient. It doesn't hurt if the token is attached (or empty).
    const response = await axios.post(`${API_URL}/auth/login`, { username, password });
    return response.data;
};

export const register = async (username, email, password, role, organizationName) => {
    const response = await axios.post(`${API_URL}/auth/register`, { username, email, password, role, organizationName });
    return response.data;
};

export const getRules = async () => {
    const response = await apiClient.get(`/rules/`);
    return response.data;
};

export const updateRule = async (id, data) => {
    const response = await apiClient.put(`/rules/${id}`, data);
    return response.data;
};
