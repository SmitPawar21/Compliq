import { useState, useEffect } from 'react';
import { getDocuments, generateComplianceReport } from '../services/api';

const ReportPage = () => {
    const [documents, setDocuments] = useState([]);
    const [contractId, setContractId] = useState('');
    const [poId, setPoId] = useState('');
    const [invoiceId, setInvoiceId] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [report, setReport] = useState(null);

    useEffect(() => {
        const fetchDocuments = async () => {
            try {
                const data = await getDocuments();
                setDocuments(data || []);
            } catch (err) {
                console.error('Failed to fetch documents', err);
            }
        };
        fetchDocuments();
    }, []);

    const contracts = documents.filter(doc => doc.documentType === 'CONTRACT');
    const pos = documents.filter(doc => doc.documentType === 'PURCHASE_ORDER');
    const invoices = documents.filter(doc => doc.documentType === 'INVOICE');

    const handleGenerate = async (e) => {
        e.preventDefault();
        if (!contractId || !poId || !invoiceId) {
            setError('Please select a Contract, Purchase Order, and Invoice.');
            return;
        }

        setLoading(true);
        setError(null);
        setReport(null);

        try {
            const data = await generateComplianceReport({
                contractDocumentId: parseInt(contractId),
                poDocumentId: parseInt(poId),
                invoiceDocumentId: parseInt(invoiceId)
            });
            setReport(data);
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Failed to generate compliance report.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <div className="mb-10 bg-white shadow overflow-hidden sm:rounded-lg">
                <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Generate Compliance Report</h3>
                    <p className="mt-1 max-w-2xl text-sm text-gray-500">Select the documents to compare and generate a comprehensive report.</p>
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

                    <form onSubmit={handleGenerate} className="space-y-6">
                        <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-3">
                            <div>
                                <label htmlFor="contract" className="block text-sm font-medium text-gray-700">Contract</label>
                                <select
                                    id="contract"
                                    value={contractId}
                                    onChange={(e) => setContractId(e.target.value)}
                                    className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md border"
                                >
                                    <option value="">Select a Contract</option>
                                    {contracts.map(doc => (
                                        <option key={doc.docId} value={doc.docId}>{doc.fileName} (ID: {doc.docId})</option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label htmlFor="po" className="block text-sm font-medium text-gray-700">Purchase Order</label>
                                <select
                                    id="po"
                                    value={poId}
                                    onChange={(e) => setPoId(e.target.value)}
                                    className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md border"
                                >
                                    <option value="">Select a PO</option>
                                    {pos.map(doc => (
                                        <option key={doc.docId} value={doc.docId}>{doc.fileName} (ID: {doc.docId})</option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label htmlFor="invoice" className="block text-sm font-medium text-gray-700">Invoice</label>
                                <select
                                    id="invoice"
                                    value={invoiceId}
                                    onChange={(e) => setInvoiceId(e.target.value)}
                                    className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md border"
                                >
                                    <option value="">Select an Invoice</option>
                                    {invoices.map(doc => (
                                        <option key={doc.docId} value={doc.docId}>{doc.fileName} (ID: {doc.docId})</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="flex justify-end">
                            <button
                                type="submit"
                                disabled={loading}
                                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                            >
                                {loading && (
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                )}
                                {loading ? 'Generating...' : 'Generate Compliance Report'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {report && (
                <div className="space-y-8 animate-fade-in-up">
                    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                        <div className="px-4 py-5 sm:px-6 border-b border-gray-200 bg-red-50">
                            <h3 className="text-lg leading-6 font-medium text-red-800">Validation Violations</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6">
                            {report.validationViolations && report.validationViolations.length > 0 ? (
                                <ul className="list-disc pl-5 space-y-2 text-sm text-gray-700">
                                    {report.validationViolations.map((violation, idx) => (
                                        <li key={idx}>
                                            <span className="font-semibold">{violation.ruleName || violation.type}:</span> {violation.description || violation.message}
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="text-sm text-gray-500">No violations found.</p>
                            )}
                        </div>
                    </div>

                    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                        <div className="px-4 py-5 sm:px-6 border-b border-gray-200 bg-blue-50">
                            <h3 className="text-lg leading-6 font-medium text-blue-800">Contract Summary</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6 text-sm text-gray-700">
                            {report.contractSummary || "No summary provided."}
                        </div>
                    </div>

                    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                        <div className="px-4 py-5 sm:px-6 border-b border-gray-200 bg-purple-50">
                            <h3 className="text-lg leading-6 font-medium text-purple-800">Clause Analysis</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6">
                            {report.clauseAnalysis && report.clauseAnalysis.length > 0 ? (
                                <ul className="space-y-4">
                                    {report.clauseAnalysis.map((clause, idx) => (
                                        <li key={idx} className="bg-gray-50 p-4 rounded-md">
                                            <h4 className="font-medium text-gray-900">{clause.clauseName || `Clause ${idx + 1}`}</h4>
                                            <p className="mt-1 text-sm text-gray-600">{clause.analysis || clause.description}</p>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="text-sm text-gray-500">No clause analysis available.</p>
                            )}
                        </div>
                    </div>

                    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                        <div className="px-4 py-5 sm:px-6 border-b border-gray-200 bg-yellow-50">
                            <h3 className="text-lg leading-6 font-medium text-yellow-800">Risk Assessment</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6">
                            <p className="text-sm text-gray-700">{report.riskAssessment || "No risk assessment available."}</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReportPage;
