import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import NavBar from "./components/NavBar";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import EmployeeDashboard from "./pages/EmployeeDashboard";
import ApplyLeaveForm from "./pages/ApplyLeaveForm";
import ManagerApprovalPage from "./pages/ManagerApprovalPage";
import ReportsPage from "./pages/ReportsPage";
import NotFound from "./pages/NotFound";

function App() {
  // Get current user role from localStorage
  const userRole = localStorage.getItem("role");

  return (
    <div style={{background: '#f8fafc', minHeight: '100vh'}}>
      <NavBar />
      <div className="main-content">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                {/* Managers are redirected to approvals page, employees see dashboard */}
                {userRole === "MANAGER" 
                  ? <Navigate to="/manager-approvals" replace />
                  : <EmployeeDashboard />
                }
              </ProtectedRoute>
            } 
          />
          <Route path="/apply-leave" element={<ProtectedRoute><ApplyLeaveForm /></ProtectedRoute>} />
          <Route path="/manager-approvals" element={<ProtectedRoute requiredRole="MANAGER"><ManagerApprovalPage /></ProtectedRoute>} />
          <Route path="/reports" element={<ProtectedRoute requiredRole="ADMIN"><ReportsPage /></ProtectedRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
