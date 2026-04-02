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
          <a href="http://www.localhost:3000/" className="icon_text">Dashboard</a>
        </p>
        <p>
          <span>
            <i className="fa-solid fa-chart-line"></i>
          </span>
          <a href="http://www.localhost:3000/reports" className="icon_text">Reports</a>
        </p>
      </div>

      <hr></hr>

      <div className="logout">

        <button>
          <i className="fa-solid fa-power-off"></i>
          <a href="http://www.localhost:3000/" className="icon_text">Logout</a>
        </button>
      </div>

    </aside>
  )
}

export default BlueColumn