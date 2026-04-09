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

        <footer>Footer</footer>

      </div>
      
    </div>
  )
}



export default App;