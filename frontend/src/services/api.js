import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const uploadDocument = async (file, documentType, uploadedByUserId = 1) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    formData.append('uploadedByUserId', uploadedByUserId);

    const response = await axios.post(`${API_URL}/documents/upload`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

export const getDocuments = async () => {
    const response = await axios.get(`${API_URL}/documents/`);
    return response.data;
};

export const generateComplianceReport = async (data) => {
    const response = await axios.post(`${API_URL}/compliance-report`, data);
    return response.data;
};
