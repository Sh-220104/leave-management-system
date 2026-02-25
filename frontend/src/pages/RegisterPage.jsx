import React, { useState } from "react";
import axios from "../api";
import { useNavigate } from "react-router-dom";

function RegisterPage() {
  const [form, setForm] = useState({ email: "", password: "", name: "", role: "EMPLOYEE" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };
  const handleSubmit = async e => {
    e.preventDefault();
    setError("");
    try {
      const resp = await axios.post("/auth/register", form);
      // Save user id on successful registration
      localStorage.setItem("userId", resp.data.id);
      localStorage.setItem("role", resp.data.roles ? Array.from(resp.data.roles)[0] : "EMPLOYEE");
      localStorage.setItem("email", resp.data.email);
      localStorage.setItem("name", resp.data.name);
      navigate("/"); // direct to dashboard after register
    } catch (e) {
      setError(e.response?.data?.message || "Registration failed.");
    }
  };

  return (
    <div className="auth-container">
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" name="name" placeholder="Full Name" value={form.name} onChange={handleChange} required />
        <input type="email" name="email" placeholder="Email" value={form.email} onChange={handleChange} required />
        <input type="password" name="password" placeholder="Password" value={form.password} onChange={handleChange} required />
        <select name="role" value={form.role} onChange={handleChange} required>
          <option value="EMPLOYEE">Employee</option>
          <option value="MANAGER">Manager</option>
          <option value="ADMIN">Admin</option>
        </select>
        <button type="submit">Register</button>
      </form>
      {error && <div className="error-msg">{error}</div>}
    </div>
  );
}

export default RegisterPage;
