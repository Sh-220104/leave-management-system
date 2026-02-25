import React from "react";

function LeaveBalanceTable({ balances = [] }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Type</th>
          <th>Balance</th>
        </tr>
      </thead>
      <tbody>
        {balances.map(b => (
          <tr key={b.leaveTypeId || b.leaveType?.id}>
            <td>{b.leaveType?.type || b.leaveTypeId}</td>
            <td>{b.balance}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
export default LeaveBalanceTable;
