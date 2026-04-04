import BlueColumn from './components/BlueColumn.jsx'
import Navbar from './components/Navbar.jsx'
import FormCampaign from './components/FormCampaign.jsx'
import PayloadPreview from './components/PayloadPreview.jsx'
function App() {
    
  return (
       
    <div className="page">
      <BlueColumn />

      <div className="content">
        
        <header>
            <Navbar />
        </header>
        

        <main>
          <FormCampaign />
          <PayloadPreview />
        </main>

        <footer>Footer</footer>

      </div>
      
    </div>
  )
}



export default App;