import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import CompliqPage from "./pages/CompliqPage";
import ReportPage from "./pages/ReportPage";

function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/compliq-platform" element={<CompliqPage />} />
          <Route path="/compliance-report" element={<ReportPage />} />
        </Routes>
      </Router>
    </>
  )
}

export default App
