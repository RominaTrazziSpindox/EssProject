import BlueColumn from './components/BlueColumn.jsx'
import Navbar from './components/Navbar.jsx'
import CampaignPage from './components/CampaignPage.jsx'


function App() {
    
  return (
       
    <div className="page">
      <BlueColumn />

      <div className="content">
        
        <header>
            <Navbar />
        </header>

        <main>
          <CampaignPage />
        </main>

        <footer>
          <p>&copy; 2026 ESS School of Management. All rights reserved.</p>
        </footer>

      </div>
      
    </div>
  )
}



export default App;