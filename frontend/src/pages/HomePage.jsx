import { Link } from 'react-router-dom';

const HomePage = () => {
    return (
        <div className="min-h-[80vh] flex flex-col items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-4xl text-center space-y-8">
                <h1 className="text-5xl font-extrabold text-gray-900 tracking-tight sm:text-6xl">
                    Welcome to <span className="text-indigo-600">Compliq</span>
                </h1>
                
                <p className="mt-4 max-w-2xl text-xl text-gray-500 mx-auto">
                    The intelligent platform for automated document compliance and risk assessment. 
                    Streamline your workflow by comparing Contracts, Purchase Orders, and Invoices with AI-driven insights.
                </p>

                <div className="bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-100 mt-10 text-left">
                    <div className="px-6 py-8 sm:p-10">
                        <h3 className="text-2xl font-bold text-gray-900 mb-6">Key Features</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div className="flex items-start">
                                <div className="flex-shrink-0">
                                    <span className="flex items-center justify-center h-10 w-10 rounded-md bg-indigo-500 text-white text-xl">📄</span>
                                </div>
                                <div className="ml-4">
                                    <h4 className="text-lg font-medium text-gray-900">Document Management</h4>
                                    <p className="mt-2 text-base text-gray-500">Securely upload and manage your critical business documents in one centralized location.</p>
                                </div>
                            </div>
                            <div className="flex items-start">
                                <div className="flex-shrink-0">
                                    <span className="flex items-center justify-center h-10 w-10 rounded-md bg-indigo-500 text-white text-xl">🤖</span>
                                </div>
                                <div className="ml-4">
                                    <h4 className="text-lg font-medium text-gray-900">AI-Powered Analysis</h4>
                                    <p className="mt-2 text-base text-gray-500">Automatically extract clauses, summarize obligations, and detect missing legal terms.</p>
                                </div>
                            </div>
                            <div className="flex items-start">
                                <div className="flex-shrink-0">
                                    <span className="flex items-center justify-center h-10 w-10 rounded-md bg-indigo-500 text-white text-xl">⚠️</span>
                                </div>
                                <div className="ml-4">
                                    <h4 className="text-lg font-medium text-gray-900">Risk Assessment</h4>
                                    <p className="mt-2 text-base text-gray-500">Identify financial, compliance, and operational risks instantly before signing.</p>
                                </div>
                            </div>
                            <div className="flex items-start">
                                <div className="flex-shrink-0">
                                    <span className="flex items-center justify-center h-10 w-10 rounded-md bg-indigo-500 text-white text-xl">✅</span>
                                </div>
                                <div className="ml-4">
                                    <h4 className="text-lg font-medium text-gray-900">Automated Validation</h4>
                                    <p className="mt-2 text-base text-gray-500">Cross-reference invoices and purchase orders against contracts to ensure alignment.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="px-6 py-6 bg-gray-50 sm:px-10 flex justify-center border-t border-gray-100 gap-4">
                        <Link
                            to="/login"
                            className="inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 md:py-4 md:text-lg md:px-10 shadow-sm transition-colors"
                        >
                            Log In
                        </Link>
                        <Link
                            to="/register"
                            className="inline-flex items-center justify-center px-8 py-3 border border-gray-300 text-base font-medium rounded-md text-indigo-700 bg-white hover:bg-gray-50 md:py-4 md:text-lg md:px-10 shadow-sm transition-colors"
                        >
                            Sign Up
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HomePage;
