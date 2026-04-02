function Navbar() {


  return (
    <div className="navbar_container">

      <div>
        <h1>Campaigns Generator</h1>
      </div>

      <ul className="navlinks">
        <li>
           <a href="http://localhost:3000">Home</a>
        </li>

        <li>
          <a href="http://localhost:3000/report">Campaigns</a>
        </li>

        <li>
          <a href="http://localhost:3000/report">Reports</a>
        </li>

        <li>
          <a href="http://localhost:3000/analytics">Analytics</a>
        </li>

        <li>
          <a href="http://localhost:3000/analytics">Support</a>
        </li>
      </ul>

      <div className="person">
        <i className="fa-regular fa-user"></i>
        <span>Admin</span>
          <a href="http://localhost:3000">
            <i className="fa-solid fa-chevron-down"></i>
          </a>
      </div>
      
    </div>
  )
}

export default Navbar