function Button({text, handleClick, variant = 'primary', type="button"}) {

    return (

    <button className={`button button--${variant}`} onClick={handleClick} type={type}>
      {text}
    </button>

    )
}

export default Button