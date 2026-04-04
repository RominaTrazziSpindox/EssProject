function Toggle() {

    return (

        <label className="toggle_switch" htmlFor="is_companion">IsCompanion
            <input type="checkbox" id="is_companion" role="switch"></input>
            <span className="slider"></span>
        </label>
    )
}

export default Toggle