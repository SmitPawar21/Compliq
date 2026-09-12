import React, { useState, useEffect } from 'react';
import { 
    getDashboardMetrics, 
    getDashboardTraces, 
    getDashboardApprovals,
    evaluateTrace 
} from '../services/dashboardService';

const DashboardPage = () => {
    const [metrics, setMetrics] = useState(null);
    const [traces, setTraces] = useState([]);
    const [approvals, setApprovals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('traces'); // traces, approvals

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        setLoading(true);
        try {
            const [metricsRes, tracesRes, approvalsRes] = await Promise.all([
                getDashboardMetrics(),
                getDashboardTraces(),
                getDashboardApprovals()
            ]);
            setMetrics(metricsRes);
            setTraces(tracesRes);
            setApprovals(approvalsRes);
        } catch (error) {
            console.error('Failed to fetch dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleEvaluate = async (traceId) => {
        try {
            await evaluateTrace(traceId);
            alert('Evaluation completed and saved!');
            fetchDashboardData();
        } catch (error) {
            console.error('Evaluation failed:', error);
            alert('Evaluation failed. Check console.');
        }
    };

    if (loading) return <div className="p-8 text-center text-gray-500">Loading dashboard...</div>;

    return (
        <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-8">AI Observability Dashboard</h1>

            {/* Metrics Overview */}
            {metrics && (
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
                        <h3 className="text-sm font-medium text-gray-500">Total Traces</h3>
                        <p className="mt-2 text-3xl font-bold text-gray-900">{metrics.totalTraces}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
                        <h3 className="text-sm font-medium text-gray-500">Total Tokens</h3>
                        <p className="mt-2 text-3xl font-bold text-gray-900">{metrics.totalTokens.toLocaleString()}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
                        <h3 className="text-sm font-medium text-gray-500">Estimated Cost</h3>
                        <p className="mt-2 text-3xl font-bold text-gray-900">${metrics.totalCost.toFixed(4)}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
                        <h3 className="text-sm font-medium text-gray-500">Avg Latency</h3>
                        <p className="mt-2 text-3xl font-bold text-gray-900">{metrics.avgLatency.toFixed(0)} ms</p>
                    </div>
                </div>
            )}

            {/* Tabs */}
            <div className="border-b border-gray-200 mb-6">
                <nav className="-mb-px flex space-x-8">
                    <button
                        onClick={() => setActiveTab('traces')}
                        className={`${
                            activeTab === 'traces'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                        } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm`}
                    >
                        Execution Traces
                    </button>
                    <button
                        onClick={() => setActiveTab('approvals')}
                        className={`${
                            activeTab === 'approvals'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                        } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm`}
                    >
                        Human Approvals
                    </button>
                </nav>
            </div>

            {/* Traces Table */}
            {activeTab === 'traces' && (
                <div className="bg-white shadow overflow-hidden sm:rounded-md">
                    <ul className="divide-y divide-gray-200">
                        {traces.map((trace) => (
                            <li key={trace.traceId}>
                                <div className="px-4 py-4 sm:px-6">
                                    <div className="flex items-center justify-between">
                                        <p className="text-sm font-medium text-blue-600 truncate">
                                            Req ID: {trace.requestId}
                                        </p>
                                        <div className="ml-2 flex-shrink-0 flex space-x-2">
                                            {trace.failures ? (
                                                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                                                    Failed
                                                </span>
                                            ) : (
                                                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                                                    Success
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                    <div className="mt-2 sm:flex sm:justify-between">
                                        <div className="sm:flex flex-col">
                                            <p className="flex items-center text-sm text-gray-500">
                                                Tokens: {trace.promptTokens + trace.completionTokens}
                                            </p>
                                            <p className="flex items-center text-sm text-gray-500 mt-1">
                                                Latency: {trace.latencyMs}ms
                                            </p>
                                            <div className="mt-2 text-sm text-gray-700 max-w-2xl truncate">
                                                <strong>Response:</strong> {trace.finalResponsePreview || 'None'}
                                            </div>
                                        </div>
                                        <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                                            <p>
                                                {new Date(trace.createdAt).toLocaleString()}
                                            </p>
                                            <button 
                                                onClick={() => handleEvaluate(trace.traceId)}
                                                className="ml-4 bg-white border border-gray-300 rounded-md shadow-sm px-3 py-1 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none"
                                            >
                                                Run Eval
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </li>
                        ))}
                        {traces.length === 0 && (
                            <li className="px-4 py-8 text-center text-gray-500">No execution traces found.</li>
                        )}
                    </ul>
                </div>
            )}

            {/* Approvals Table */}
            {activeTab === 'approvals' && (
                <div className="bg-white shadow overflow-hidden sm:rounded-md">
                    <ul className="divide-y divide-gray-200">
                        {approvals.map((approval) => (
                            <li key={approval.id}>
                                <div className="px-4 py-4 sm:px-6">
                                    <div className="flex items-center justify-between">
                                        <p className="text-sm font-medium text-gray-900">
                                            {approval.actionDescription}
                                        </p>
                                        <div className="ml-2 flex-shrink-0 flex">
                                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                                ${approval.status === 'APPROVED' ? 'bg-green-100 text-green-800' : 
                                                  approval.status === 'REJECTED' ? 'bg-red-100 text-red-800' : 
                                                  'bg-yellow-100 text-yellow-800'}`}>
                                                {approval.status}
                                            </span>
                                        </div>
                                    </div>
                                    <div className="mt-2 sm:flex sm:justify-between">
                                        <div className="sm:flex">
                                            <p className="flex items-center text-sm text-gray-500">
                                                Token: {approval.approvalToken}
                                            </p>
                                        </div>
                                        <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                                            <p>Created: {new Date(approval.createdAt).toLocaleString()}</p>
                                        </div>
                                    </div>
                                </div>
                            </li>
                        ))}
                        {approvals.length === 0 && (
                            <li className="px-4 py-8 text-center text-gray-500">No approval logs found.</li>
                        )}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default DashboardPage;
