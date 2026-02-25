import React, { useEffect, useState } from "react";
import LeaveTable from "../components/LeaveTable";
import axios from "../api";

function ManagerApprovalPage() {
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(false);
  const [resultMsg, setResultMsg] = useState("");

  const fetchLeaves = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/leaves/pending");
      setPending(res.data);
    } catch {
      setPending([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchLeaves();
  }, []);

  const handleDecision = async (leaveId, isApprove, managerComment = "") => {
    setLoading(true);
    setResultMsg(""); // Clear previous messages
    const action = isApprove ? "approve" : "reject";
    try {
      await axios.put(`/leaves/${leaveId}/${action}`, managerComment ? { managerComment } : {});
      setResultMsg(isApprove ? "Leave Approved!" : "Leave Rejected!");
      // Optionally update state immediately
      setPending(prev =>
        prev.map(l =>
          l.id === leaveId
            ? { ...l, status: isApprove ? "APPROVED" : "REJECTED", managerComment }
            : l
        )
      );
      // Then refetch for accuracy
      await fetchLeaves();
    } catch (error) {
      setResultMsg("Error updating leave.");
    }
    setLoading(false);
  };

  return (
    <div className="dashboard-section">
      <h2>Pending Leave Requests</h2>
      {resultMsg && <div style={{ color: resultMsg.includes("Error") ? "red" : "green", marginBottom: 8 }}>{resultMsg}</div>}
      {loading ? (
        <div>Loading...</div>
      ) : pending.length > 0 ? (
        <LeaveTable leaveRequests={pending} showActions onAction={handleDecision} loading={loading} />
      ) : (
        <div>No pending leave requests.</div>
      )}
    </div>
  );
}

export default ManagerApprovalPage;
