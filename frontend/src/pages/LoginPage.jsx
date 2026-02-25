import React, { useState } from "react";
import axios from "../api";
import { useNavigate } from "react-router-dom";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async e => {
    e.preventDefault();
    setError("");
    try {
      const resp = await axios.post("/auth/login", { email, password });
      // Save backend-provided info to localStorage
      localStorage.setItem("token", resp.data.jwt);
      localStorage.setItem("userId", resp.data.employeeId); // Ensure employeeId is present
      localStorage.setItem("role", resp.data.role);
      localStorage.setItem("email", resp.data.email);
      localStorage.setItem("name", resp.data.name);
      // Redirect based on role
      if (resp.data.role === "ADMIN") {
        navigate("/reports");
      } else if (resp.data.role === "MANAGER") {
        navigate("/manager-approvals");
      } else {
        navigate("/");
      }
    } catch (e) {
      setError(e.response?.data?.message || "Login failed.");
    }
  };

  return (
    <div className="auth-container">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
        <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button type="submit">Login</button>
      </form>
      {error && <div className="error-msg">{error}</div>}
    </div>
  );
}

export default LoginPage;
