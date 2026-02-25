import React from "react";
import { Link, useNavigate } from "react-router-dom";

function NavBar() {
  const isAuthenticated = !!localStorage.getItem("token");
  const userRole = localStorage.getItem("role");
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-logo">ELMS</div>
      {isAuthenticated ? (
        <ul className="navbar-links">
          {/* Only show dashboard to employees */}
          {userRole === "EMPLOYEE" && <li><Link to="/">Dashboard</Link></li>}
          {userRole === "EMPLOYEE" && <li><Link to="/apply-leave">Apply Leave</Link></li>}
          {userRole === "MANAGER" && <li><Link to="/manager-approvals">Approvals</Link></li>}
          {userRole === "ADMIN" && <li><Link to="/reports">Reports</Link></li>}
          <li><button className="logout-btn" onClick={logout}>Logout</button></li>
        </ul>
      ) : (
        <ul className="navbar-links">
          <li><Link to="/login">Login</Link></li>
          <li><Link to="/register">Register</Link></li>
        </ul>
      )}
    </nav>
  );
}

export default NavBar;
