import React, { useState, useEffect } from "react";
import axios from "./api";

function ApplyLeaveForm() {
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [form, setForm] = useState({
    leaveTypeId: "",
    startDate: "",
    endDate: "",
    reason: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    axios
      .get("/leave-types")
      .then((res) => setLeaveTypes(res.data))
      .catch(() => setLeaveTypes([]));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    // You must get employeeId from localStorage or context
    const employeeId = localStorage.getItem("userId");
    if (!employeeId) {
      setError("Not logged in.");
      return;
    }
    try {
      await axios.post(
        "/leaves/apply",
        {
          employeeId: Number(employeeId),
          leaveTypeId: Number(form.leaveTypeId),
          startDate: form.startDate,
          endDate: form.endDate,
          reason: form.reason,
        },
        {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        }
      );
      setSuccess("Leave applied successfully!");
      setForm({ leaveTypeId: "", startDate: "", endDate: "", reason: "" });
    } catch (err) {
      setError("Error applying for leave. Check the form and try again.");
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: 400, margin: "0 auto" }}>
      <div style={{ marginBottom: 16 }}>
        <select
          name="leaveTypeId"
          value={form.leaveTypeId}
          onChange={handleChange}
          required
        >
          <option value="">Select Leave Type</option>
          {leaveTypes.map((lt) => (
            <option key={lt.id} value={lt.id}>{lt.name}</option>
          ))}
        </select>
      </div>
      <div style={{ marginBottom: 16 }}>
        <input
          type="date"
          name="startDate"
          value={form.startDate}
          onChange={handleChange}
          required
        />
      </div>
      <div style={{ marginBottom: 16 }}>
        <input
          type="date"
          name="endDate"
          value={form.endDate}
          onChange={handleChange}
          required
        />
      </div>
      <div style={{ marginBottom: 16 }}>
        <input
          type="text"
          name="reason"
          placeholder="Reason for leave"
          value={form.reason}
          onChange={handleChange}
          required
        />
      </div>
      <button type="submit">Apply</button>
      {error && <div style={{ color: "red", marginTop: 8 }}>{error}</div>}
      {success && <div style={{ color: "green", marginTop: 8 }}>{success}</div>}
    </form>
  );
}

export default ApplyLeaveForm;
