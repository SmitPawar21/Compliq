import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
    const location = useLocation();
    
    const isActive = (path) => location.pathname === path ? 'text-indigo-600 bg-indigo-50 border-indigo-600' : 'text-gray-600 hover:text-indigo-600 hover:bg-gray-50 border-transparent';

    return (
        <nav className="bg-white shadow-sm border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex">
                        <div className="flex-shrink-0 flex items-center">
                            <span className="text-2xl font-bold text-indigo-600 tracking-tight">Compliq</span>
                        </div>
                        <div className="hidden sm:-my-px sm:ml-8 sm:flex sm:space-x-8">
                            <Link
                                to="/upload"
                                className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors ${isActive('/upload')}`}
                            >
                                Upload Document
                            </Link>
                            <Link
                                to="/report"
                                className={`inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors ${isActive('/report')}`}
                            >
                                Compliance Report
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
