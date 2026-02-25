import React, { useEffect, useState } from "react";
import axios from "../api";

function ReportsPage() {
  const [reports, setReports] = useState([]);
  useEffect(() => {
    axios.get("/reports/leaves")
      .then(res => setReports(res.data))
      .catch(() => setReports([]));
  }, []);

  return (
    <div className="dashboard-container">
      <h2>Leave Reports</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Employee</th>
            <th>Leave Type</th>
            <th>Start Date</th>
            <th>End Date</th>
            <th>Status</th>
            <th>Reason</th>
            <th>Manager Comment</th>
          </tr>
        </thead>
        <tbody>
          {reports.map((lr) => (
            <tr key={lr.id}>
              <td>{lr.employee?.name || lr.employeeId}</td>
              <td>{lr.leaveType?.type || lr.leaveTypeId}</td>
              <td>{lr.startDate}</td>
              <td>{lr.endDate}</td>
              <td>{lr.status}</td>
              <td>{lr.reason || lr.notes}</td>
              <td>{lr.managerComment || ""}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ReportsPage;
