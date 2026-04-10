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