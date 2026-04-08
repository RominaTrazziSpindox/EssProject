function Toggle({ id, checked, onChange, disabled }) {
    return (
        <>
            <label htmlFor={id}>Is companion</label>
            <input
                id={id}
                role="switch"
                type="checkbox"
                checked={checked}
                onChange={onChange}
                disabled={disabled}
            />
        </>
    );
}

export default Toggle;