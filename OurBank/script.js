const contentData = {
    home: `
        <div class="inner-content">
            <h2>Welcome to OurBank</h2>
            <p>Experience seamless and secure financial services. Banking at your doorstep.</p>
        </div>
    `,
    about: `
        <div class="inner-content">
            <h2>About Us</h2>
            <p>OurBank has been a trusted financial partner since 1995. We are committed to providing innovative banking solutions and securing your financial future.</p>
        </div>
    `,
    services: `
        <div class="inner-content">
            <div class="services-list">
                <a href="loan.html">LOAN</a> &nbsp;
                <a href="deposit.html">DEPOSIT</a> &nbsp;
                <a href="account.html">AC OPENING</a>
            </div>
        </div>
    `,
    netbanking: `
        <div class="inner-content">
            <div class="banking-toggle">
                <a href="login.html">LOGIN</a> 
                <a href="signup.html">SIGN UP</a>
            </div>
        </div>
    `,
    contact: `
        <div class="inner-content">
            <h2>Contact Us</h2>
            <p><strong>Address:</strong> 456 Financial Hub, Sector 4, New City</p>
            <p><strong>Phone:</strong> 1800-123-4567</p>
            <p><strong>Email:</strong> support@ourbank.com</p>
        </div>
    `
};

function loadContent(section, element) {
    document.getElementById('content-area').innerHTML = contentData[section];

    let menuItems = document.querySelectorAll('#menu li');
    menuItems.forEach(item => item.classList.remove('active'));
    element.classList.add('active');
}

window.onload = function() {
    document.getElementById('content-area').innerHTML = contentData.home;
};