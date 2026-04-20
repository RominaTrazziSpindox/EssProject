import PropTypes from 'prop-types';

Input.propTypes = {
    label: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    type: PropTypes.string,
    placeholder: PropTypes.string,
    value: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.number,
    ]).isRequired,
    onChange: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
    error: PropTypes.string,
};



function Input({label, id, name, type = 'text', placeholder, value, onChange, disabled = false, error = ''}) {
  
  return (
    <>
      <label htmlFor={id}>{label}</label>
      <input type={type} id={id} name={name} placeholder={placeholder} value={value} onChange={onChange} disabled={disabled}/>
      {error && <small className="input_error">{error}</small>}
    </>
  );
}

export default Input;