import { useState, useEffect } from 'react';
import { uploadDocument, getDocuments, getAllUsers, getRules, updateRule } from '../services/api';
import { getCookie } from '../utils/cookies';

const UploadPage = () => {
    const [file, setFile] = useState(null);
    const [documentType, setDocumentType] = useState('CONTRACT');
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [fetchLoading, setFetchLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const [rules, setRules] = useState([]);
    const [rulesLoading, setRulesLoading] = useState(false);

    const fetchDocuments = async () => {
        setFetchLoading(true);
        try {
            const data = await getDocuments();
            setDocuments(data);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch documents', err);
            setError('Failed to load documents.');
        } finally {
            setFetchLoading(false);
        }
    };

    const fetchRules = async () => {
        setRulesLoading(true);
        try {
            const data = await getRules();
            setRules(data);
        } catch (err) {
            console.error('Failed to fetch rules', err);
        } finally {
            setRulesLoading(false);
        }
    };

    useEffect(() => {
        fetchDocuments();
        fetchRules();
    }, []);

    const handleRuleToggle = async (ruleId, currentStatus) => {
        const ruleToUpdate = rules.find(r => r.id === ruleId);
        if (!ruleToUpdate) return;
        try {
            await updateRule(ruleId, { ...ruleToUpdate, enabled: !currentStatus });
            fetchRules();
        } catch (err) {
            console.error('Failed to update rule', err);
            alert('Failed to update rule. Make sure you are an ADMIN.');
        }
    };

    const handleRuleValueChange = async (ruleId, newValue) => {
        const ruleToUpdate = rules.find(r => r.id === ruleId);
        if (!ruleToUpdate) return;
        try {
            await updateRule(ruleId, { ...ruleToUpdate, conditionValue: newValue });
            fetchRules();
        } catch (err) {
            console.error('Failed to update rule', err);
            alert('Failed to update rule. Make sure you are an ADMIN.');
        }
    };

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
            // Find logged-in user's ID
            const loggedInUsername = getCookie('username');
            const usersResponse = await getAllUsers();
            const usersList = usersResponse.users || [];
            const currentUser = usersList.find(u => u.username === loggedInUsername);
            
            if (!currentUser) {
                throw new Error("Could not find your user ID on the server.");
            }

            await uploadDocument(file, documentType, currentUser.id);

            setSuccessMessage('Document uploaded successfully!');
            setFile(null);
            document.getElementById('file-upload').value = '';
            fetchDocuments(); // Refresh table
        } catch (err) {
            let errorMessage = 'Failed to upload document.';
            if (err.response?.data) {
                errorMessage = typeof err.response.data === 'string' 
                    ? err.response.data 
                    : err.response.data.message || JSON.stringify(err.response.data);
            } else if (err.message) {
                errorMessage = err.message;
            }
            setError(errorMessage);
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
                                                <tr key={doc.doc_id || doc.docId}>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                                        {doc.doc_id || doc.docId}
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

            {/* Compliance Rule Settings */}
            <div className="bg-white shadow overflow-hidden sm:rounded-lg mt-8">
                <div className="px-4 py-5 sm:px-6 border-b border-gray-200 flex justify-between items-center">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Compliance Rule Settings</h3>
                    <button onClick={fetchRules} className="text-indigo-600 hover:text-indigo-900 text-sm font-medium">Refresh</button>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    {rulesLoading ? (
                        <p className="text-sm text-gray-500">Loading rules...</p>
                    ) : rules.length === 0 ? (
                        <p className="text-sm text-gray-500">No rule configurations found for your organization.</p>
                    ) : (
                        <div className="space-y-6">
                            {rules.map((rule) => (
                                <div key={rule.id} className="flex items-center justify-between border-b border-gray-100 pb-4 last:border-b-0 last:pb-0">
                                    <div className="flex-1">
                                        <h4 className="text-sm font-medium text-gray-900">{rule.ruleName}</h4>
                                        <p className="text-sm text-gray-500 mt-1">Severity: {rule.severity}</p>
                                    </div>
                                    <div className="flex items-center space-x-4">
                                        <div className="flex items-center">
                                            <label htmlFor={`conditionValue-${rule.id}`} className="sr-only">Value</label>
                                            <input
                                                id={`conditionValue-${rule.id}`}
                                                type="text"
                                                defaultValue={rule.conditionValue}
                                                onBlur={(e) => {
                                                    if (e.target.value !== rule.conditionValue) {
                                                        handleRuleValueChange(rule.id, e.target.value);
                                                    }
                                                }}
                                                className="shadow-sm focus:ring-indigo-500 focus:border-indigo-500 block w-24 sm:text-sm border-gray-300 rounded-md py-1.5 px-3 border"
                                                title="Condition Value"
                                            />
                                        </div>
                                        <button
                                            onClick={() => handleRuleToggle(rule.id, rule.enabled)}
                                            className={`${
                                                rule.enabled ? 'bg-indigo-600' : 'bg-gray-200'
                                            } relative inline-flex flex-shrink-0 h-6 w-11 border-2 border-transparent rounded-full cursor-pointer transition-colors ease-in-out duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500`}
                                        >
                                            <span className="sr-only">Use setting</span>
                                            <span
                                                aria-hidden="true"
                                                className={`${
                                                    rule.enabled ? 'translate-x-5' : 'translate-x-0'
                                                } pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow transform ring-0 transition ease-in-out duration-200`}
                                            />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default UploadPage;
