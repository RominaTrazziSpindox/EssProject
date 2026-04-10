function Button({text, onClick, variant = 'primary', type="button", disabled = false}) {

    return (

    <button className={`button button--${variant}`} onClick={onClick} type={type}>
      {text}
    </button>

    )
}

export default Button