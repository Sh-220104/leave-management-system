import React, { useState, useEffect } from "react";
import axios from "../api";
import { useNavigate } from "react-router-dom";

function ApplyLeaveForm() {
  const [form, setForm] = useState({ leaveTypeId: "", startDate: "", endDate: "", reason: "" });
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    axios.get("/leave-types").then(res => setLeaveTypes(res.data));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await axios.post("/leaves/apply", {
        employeeId: Number(localStorage.getItem("userId")),
        ...form,
      });
      navigate("/");
    } catch (e) {
      // Automatically handle 401 Unauthorized: prompt user to login
      if (e.response && e.response.status === 401) {
        setError("Session expired or unauthorized. Please login again.");
        navigate("/login");
      } else {
        setError(e.response?.data?.message || "Leave application failed.");
      }
    }
  };

  return (
    <div className="form-container">
      <h2>Apply for Leave</h2>
      <form onSubmit={handleSubmit}>
        <select name="leaveTypeId" value={form.leaveTypeId} required onChange={handleChange}>
          <option value="">Select Leave Type</option>
          {leaveTypes.map(lt => (
            <option key={lt.id} value={lt.id}>{lt.type}</option>
          ))}
        </select>
        <input type="date" name="startDate" value={form.startDate} required onChange={handleChange} />
        <input type="date" name="endDate" value={form.endDate} required onChange={handleChange} />
        <input type="text" name="reason" value={form.reason} onChange={handleChange} required placeholder="Reason" />
        <button type="submit">Apply</button>
      </form>
      {error && <div className="error-msg">{error}</div>}
    </div>
  );
}

export default ApplyLeaveForm;
