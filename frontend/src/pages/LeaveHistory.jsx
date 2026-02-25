import React, { useEffect, useState } from "react";
import axios from "../api/axios";

const LeaveHistory = () => {
  const user = JSON.parse(localStorage.getItem("user"));
  const [history, setHistory] = useState([]);
  const [filterType, setFilterType] = useState("");
  const [filterDate, setFilterDate] = useState("");

  useEffect(() => {
    axios.get(`/leaves/employee/${user.id}`).then(res => setHistory(res.data));
  }, [user.id]);

  const filtered = history.filter(lr =>
    (!filterType || lr.leaveType.type === filterType) &&
    (!filterDate || lr.startDate >= filterDate || lr.endDate >= filterDate)
  );

  return (
    <div>
      <h3>Your Leave History</h3>
      <div>
        <label>Filter by Type:</label>
        <input value={filterType} onChange={e=>setFilterType(e.target.value)} />
        <label>Filter by Date:</label>
        <input type="date" value={filterDate} onChange={e=>setFilterDate(e.target.value)} />
      </div>
      <table>
        <thead>
          <tr><th>Type</th><th>From</th><th>To</th><th>Status</th><th>Notes</th></tr>
        </thead>
        <tbody>
          {filtered.map(lr => (
            <tr key={lr.id}>
              <td>{lr.leaveType.type}</td>
              <td>{lr.startDate}</td>
              <td>{lr.endDate}</td>
              <td>{lr.status}</td>
              <td>{lr.notes}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default LeaveHistory;
