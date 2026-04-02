function BlueColumn() {
  return (
    <aside className="blue-column">

      <div className="brand">
        <p className="institution_name">ESS Institute</p>
        <p className="institution_division">School of Management</p>
      </div>

      <hr></hr>
      
      <div className="control_panel">
        <p>CONTROL PANEL</p>
        <p>
          <span>
            <i className="fa-regular fa-house"></i>
          </span>
          <span className="icon_text">Dashboard</span>
        </p>
        <p>
          <span>
            <i className="fa-solid fa-chart-line"></i>
          </span>
          <span className="icon_text">Reports</span>
        </p>
      </div>

      <hr></hr>

      <button>
        <i className="fa-solid fa-power-off"></i>
        <span className="icon_text">Logout</span>
      
      </button>

    </aside>
  )
}

export default BlueColumn