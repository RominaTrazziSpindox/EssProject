function Input({label, id, type = 'text', placeholder, value, onChange, disabled = false, error = ''}) {

    return (
        <div className="fields">
            <label htmlFor={id}>{label}</label>
            <input  type={type} id={id} placeholder={placeholder} value={value} onChange={onChange} disabled={disabled}/>
                {error && <small className="input_error">{error}</small>}
        </div>
    );
}

export default Input;