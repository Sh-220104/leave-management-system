import React, { useEffect, useState } from "react";
import axios from "../api";
import LeaveBalanceTable from "../components/LeaveBalanceTable";
import LeaveTable from "../components/LeaveTable";

function EmployeeDashboard() {
  const [balances, setBalances] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const userId = localStorage.getItem("userId");
  const userRole = localStorage.getItem("role");

  useEffect(() => {
    // Only call API if userId is valid and role is EMPLOYEE
    if (!userId || userRole !== "EMPLOYEE") {
      setBalances([]);
      setLeaves([]);
      return;
    }
    axios.get(`/balance/${userId}`)
      .then(res => setBalances(res.data))
      .catch(() => setBalances([]));

    axios.get(`/leaves/employee/${userId}`)
      .then(res => setLeaves(res.data))
      .catch(() => setLeaves([]));
  }, [userId, userRole]);

  if (userRole !== "EMPLOYEE") {
    // Hide the employee dashboard completely for non-employees
    return (
      <div style={{marginTop: 40, textAlign: "center"}}>
        <h2>Not authorized to view this page.</h2>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <h2>Your Leave Dashboard</h2>
      <div className="dashboard-section">
        <h3>Your Leave Balance</h3>
        {balances && balances.length > 0
          ? (<LeaveBalanceTable balances={balances} />)
          : (<p>No leave balance data.</p>)}
      </div>
      <div className="dashboard-section">
        <h3>Your Leave Requests</h3>
        {leaves && leaves.length > 0
          ? (<LeaveTable leaveRequests={leaves} showActions={false} />)
          : (<p>No leave requests.</p>)}
      </div>
      {!userId && (
        <div style={{color: "red"}}>
          User not logged in. Please <a href="/login">login</a>.
        </div>
      )}
    </div>
  );
}

export default EmployeeDashboard;
