import PropTypes from "prop-types";

Toggle.propTypes = {
    id: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
};

function Toggle({id, checked, onChange, disabled }) {
    return (
        <>
            <label htmlFor={id}>Is companion</label>
            <input id={id} role="switch" type="checkbox" checked={checked} onChange={onChange} disabled={disabled}/> 
        </>
    );
}



export default Toggle;