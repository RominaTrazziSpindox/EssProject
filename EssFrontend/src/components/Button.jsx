function Button({text, onClick, variant = 'primary', type="button"}) {

    return (

    <button className={`button button--${variant}`} onClick={onClick} type={type}>
      {text}
    </button>

    )
}

export default Button