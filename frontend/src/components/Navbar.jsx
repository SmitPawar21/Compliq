import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getCookie, removeCookie } from '../utils/cookies';

const Navbar = () => {
    const location = useLocation();
    
    const isActive = (path) => location.pathname === path ? 'text-indigo-600 bg-indigo-50 border-indigo-600' : 'text-gray-600 hover:text-indigo-600 hover:bg-gray-50 border-transparent';

    const navigate = useNavigate();
    const token = getCookie('token');

    const handleLogout = () => {
        removeCookie('token');
        removeCookie('username');
        navigate('/login');
        window.location.reload();
    };

    return (
        <nav className="bg-white shadow-sm border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex">
                        <Link to="/" className="flex-shrink-0 flex items-center">
                            <span className="text-2xl font-bold text-indigo-600 tracking-tight">Compliq</span>
                        </Link>
                        {token && (
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
                        )}
                    </div>
                    <div className="hidden sm:flex sm:items-center sm:ml-6">
                        {token ? (
                            <button
                                onClick={handleLogout}
                                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none"
                            >
                                Log out
                            </button>
                        ) : (
                            <div className="space-x-4">
                                <Link to="/login" className="text-gray-600 hover:text-indigo-600 font-medium">Log in</Link>
                                <Link to="/register" className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none">Sign up</Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
