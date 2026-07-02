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
                setDocuments(data);
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
            const data = {
                contractDocumentId: parseInt(contractId),
                poDocumentId: parseInt(poId),
                invoiceDocumentId: parseInt(invoiceId)
            };
            const result = await generateComplianceReport(data);
            setReport(result);
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Failed to generate report.');
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
                                        <option key={doc.doc_id || doc.docId} value={doc.doc_id || doc.docId}>{doc.fileName} (ID: {doc.doc_id || doc.docId})</option>
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
                                        <option key={doc.doc_id || doc.docId} value={doc.doc_id || doc.docId}>{doc.fileName} (ID: {doc.doc_id || doc.docId})</option>
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
                                        <option key={doc.doc_id || doc.docId} value={doc.doc_id || doc.docId}>{doc.fileName} (ID: {doc.doc_id || doc.docId})</option>
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
                <div className="space-y-8 animate-fade-in-up mt-8">
                    {/* Overall Status */}
                    <div className="bg-red-50 border-l-4 border-red-500 p-6 shadow-sm rounded-lg">
                        <div className="flex items-center justify-between">
                            <h2 className="text-2xl font-bold text-gray-900 tracking-tight">COMPLIANCE REPORT</h2>
                            <span className="inline-flex items-center px-4 py-1.5 rounded-full text-sm font-semibold bg-red-200 text-red-800">
                                🔴 {report.riskAssessment?.riskLevel || 'UNKNOWN'}
                            </span>
                        </div>
                    </div>

                    {/* Validation Violations */}
                    <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
                        <div className="px-4 py-5 sm:px-6 bg-gray-50 border-b border-gray-200">
                            <h3 className="text-lg leading-6 font-semibold text-gray-900">Validation Violations</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6 text-gray-700">
                            <ul className="space-y-3">
                                {report.validationResult?.violations?.map((violation, idx) => (
                                    <li key={idx} className="flex items-start">
                                        <span className="text-red-500 mr-2">❌</span>
                                        <span>{violation.message}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>

                    {/* Contract Summary */}
                    <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
                        <div className="px-4 py-5 sm:px-6 bg-gray-50 border-b border-gray-200">
                            <h3 className="text-lg leading-6 font-semibold text-gray-900">Contract Summary</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6 grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700">
                            <div>
                                <h4 className="font-semibold text-gray-900 mb-1">Purpose:</h4>
                                <p className="mb-4">{report.contractSummary?.contractPurpose}</p>
                                
                                <h4 className="font-semibold text-gray-900 mb-1">Duration:</h4>
                                <p className="mb-4">{report.contractSummary?.contractDuration}</p>

                                <h4 className="font-semibold text-gray-900 mb-1">Payment Terms:</h4>
                                <p>{report.contractSummary?.paymentTerms}</p>
                            </div>
                            <div>
                                <h4 className="font-semibold text-gray-900 mb-2">Key Obligations:</h4>
                                <ul className="space-y-2">
                                    {report.contractSummary?.keyObligations?.map((obligation, idx) => (
                                        <li key={idx} className="flex items-start">
                                            <span className="text-gray-400 mr-2">•</span>
                                            <span>{obligation}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    </div>

                    {/* Clause Analysis */}
                    <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
                        <div className="px-4 py-5 sm:px-6 bg-gray-50 border-b border-gray-200">
                            <h3 className="text-lg leading-6 font-semibold text-gray-900">Clause Analysis</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6 grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <div>
                                <h4 className="font-semibold text-green-700 mb-3 border-b pb-2">Present</h4>
                                <ul className="space-y-2 text-gray-700">
                                    {report.clauseAnalysis?.presentClauses?.map((clause, idx) => (
                                        <li key={idx} className="flex items-center">
                                            <span className="text-green-500 mr-2">✔</span>
                                            <span>{clause}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                            <div>
                                <h4 className="font-semibold text-red-700 mb-3 border-b pb-2">Missing</h4>
                                <ul className="space-y-2 text-gray-700">
                                    {report.clauseAnalysis?.missingClauses?.map((clause, idx) => (
                                        <li key={idx} className="flex items-center">
                                            <span className="text-red-500 mr-2">✖</span>
                                            <span>{clause}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    </div>

                    {/* Risk Assessment */}
                    <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
                        <div className="px-4 py-5 sm:px-6 bg-gray-50 border-b border-gray-200">
                            <h3 className="text-lg leading-6 font-semibold text-gray-900">Risk Assessment</h3>
                        </div>
                        <div className="px-4 py-5 sm:p-6 text-gray-700">
                            <div className="mb-6">
                                <h4 className="font-semibold text-gray-900 mb-2">Risk Level</h4>
                                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold bg-red-100 text-red-800">
                                    🔴 {report.riskAssessment.riskLevel}
                                </span>
                            </div>

                            <div className="mb-6">
                                <h4 className="font-semibold text-gray-900 mb-2">Identified Risks</h4>
                                <ul className="space-y-1">
                                    {report.riskAssessment?.risks?.map((risk, idx) => (
                                        <li key={idx} className="flex items-start">
                                            <span className="text-gray-400 mr-2">•</span>
                                            <span className="text-sm">{risk}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>

                            <div className="pt-6 border-t border-gray-200">
                                <h4 className="font-semibold text-gray-900 mb-3">Recommendations</h4>
                                <ul className="space-y-2 bg-green-50 p-4 rounded-md">
                                    {report.riskAssessment?.recommendations?.map((rec, idx) => (
                                        <li key={idx} className="flex items-start text-green-900">
                                            <span className="mr-2">✔</span>
                                            <span>{rec}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReportPage;
