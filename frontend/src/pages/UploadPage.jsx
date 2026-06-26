import { useState, useEffect } from 'react';
import { uploadDocument, getDocuments } from '../services/api';

const UploadPage = () => {
    const [file, setFile] = useState(null);
    const [documentType, setDocumentType] = useState('CONTRACT');
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [fetchLoading, setFetchLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);

    const fetchDocuments = async () => {
        setFetchLoading(true);
        try {
            // const data = await getDocuments();
            const localDocs = JSON.parse(localStorage.getItem('uploadedDocuments')) || [];
            setDocuments(localDocs);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch documents', err);
            setError('Failed to load documents.');
        } finally {
            setFetchLoading(false);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, []);

    const handleFileChange = (e) => {
        if (e.target.files.length > 0) {
            setFile(e.target.files[0]);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!file) {
            setError('Please select a file to upload.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            // await uploadDocument(file, documentType);
            
            // Mocking local storage save
            const newDoc = {
                docId: Date.now(),
                fileName: file.name,
                documentType: documentType
            };
            const existingDocs = JSON.parse(localStorage.getItem('uploadedDocuments')) || [];
            localStorage.setItem('uploadedDocuments', JSON.stringify([...existingDocs, newDoc]));

            setSuccessMessage('Document uploaded successfully!');
            setFile(null);
            document.getElementById('file-upload').value = '';
            fetchDocuments(); // Refresh table
        } catch (err) {
            setError(err.message || 'Failed to upload document.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <div className="mb-10 bg-white shadow overflow-hidden sm:rounded-lg">
                <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Upload Document</h3>
                    <p className="mt-1 max-w-2xl text-sm text-gray-500">Upload a contract, purchase order, or invoice for compliance checking.</p>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    {error && (
                        <div className="mb-4 bg-red-50 border-l-4 border-red-400 p-4">
                            <div className="flex">
                                <div className="ml-3">
                                    <p className="text-sm text-red-700">{error}</p>
                                </div>
                            </div>
                        </div>
                    )}
                    {successMessage && (
                        <div className="mb-4 bg-green-50 border-l-4 border-green-400 p-4">
                            <div className="flex">
                                <div className="ml-3">
                                    <p className="text-sm text-green-700">{successMessage}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    <form onSubmit={handleUpload} className="space-y-6 sm:space-y-5">
                        <div className="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start">
                            <label htmlFor="documentType" className="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
                                Document Type
                            </label>
                            <div className="mt-1 sm:mt-0 sm:col-span-2">
                                <select
                                    id="documentType"
                                    name="documentType"
                                    value={documentType}
                                    onChange={(e) => setDocumentType(e.target.value)}
                                    className="max-w-lg block focus:ring-indigo-500 focus:border-indigo-500 w-full shadow-sm sm:max-w-xs sm:text-sm border-gray-300 rounded-md py-2 px-3 border"
                                >
                                    <option value="CONTRACT">Contract</option>
                                    <option value="PURCHASE_ORDER">Purchase Order</option>
                                    <option value="INVOICE">Invoice</option>
                                </select>
                            </div>
                        </div>

                        <div className="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start border-t border-gray-200 pt-5">
                            <label htmlFor="file-upload" className="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
                                File
                            </label>
                            <div className="mt-1 sm:mt-0 sm:col-span-2">
                                <input
                                    id="file-upload"
                                    name="file-upload"
                                    type="file"
                                    onChange={handleFileChange}
                                    className="max-w-lg block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 border border-gray-300 rounded-md py-1.5 px-3"
                                />
                            </div>
                        </div>

                        <div className="pt-5">
                            <div className="flex justify-end">
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="ml-3 inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                                >
                                    {loading ? 'Uploading...' : 'Upload'}
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                <div className="px-4 py-5 sm:px-6 border-b border-gray-200 flex justify-between items-center">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Uploaded Documents</h3>
                    <button onClick={fetchDocuments} className="text-indigo-600 hover:text-indigo-900 text-sm font-medium">Refresh</button>
                </div>
                <div className="flex flex-col">
                    <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                        <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                            <div className="overflow-hidden border-b border-gray-200">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                ID
                                            </th>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                File Name
                                            </th>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Type
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {fetchLoading ? (
                                            <tr>
                                                <td colSpan="3" className="px-6 py-4 text-center text-sm text-gray-500">
                                                    Loading documents...
                                                </td>
                                            </tr>
                                        ) : documents.length === 0 ? (
                                            <tr>
                                                <td colSpan="3" className="px-6 py-4 text-center text-sm text-gray-500">
                                                    No documents found.
                                                </td>
                                            </tr>
                                        ) : (
                                            documents.map((doc) => (
                                                <tr key={doc.docId}>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                                        {doc.docId}
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                        {doc.fileName}
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                        <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-indigo-100 text-indigo-800">
                                                            {doc.documentType}
                                                        </span>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UploadPage;
