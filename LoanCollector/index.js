function calculateEMI(){
    let name=document.getElementById("name").value;
    let type=document.getElementById("type").value;
    let amount = +document.getElementById("amount").value;
    let duration = +document.getElementById("duration").value;
    let interest;
    let res=document.getElementById("result");
    let invalid=document.getElementById("invalid");

    if(name=="" || name.length<3){
        // alert("Please enter a valid name");
        invalid.innerHTML = "Please enter a valid name";
        return;
    }

    if(type==="car"){
        interest = 9;
        document.getElementById("interest").value = "9%";
    }
    else if(type==="home"){
        interest = 12;
        document.getElementById("interest").value = "12%";
    }
    else if(type==="personal"){
        interest = 15;
        document.getElementById("interest").value = "15%";
    }

    if(type==="car" && amount<100000){
        // alert("Car loan amount should be at least 1 lakh");
        invalid.innerHTML = "Car loan amount should be at least 1 lakh";
        return;
    }
    if(type==="home" && amount<500000){
        // alert("Home loan amount should be at least 5 lakhs");
        invalid.innerHTML = "Home loan amount should be at least 5 lakhs";
        return;
    }
    if(type==="personal" && amount<10000){
        // alert("Personal loan amount should be at least 10K");
        invalid.innerHTML = "Personal loan amount should be at least 10K";
        return;
    }

    if(type==="car" && duration<7){
        invalid.innerHTML = "Car loan duration should be at least 7 years";
        return;
    }
    if(type==="home" && duration<30){
        invalid.innerHTML = "Home loan duration should be at least 30 years";
        return;
    }
    if(type==="personal" && duration<5){
        invalid.innerHTML = "Personal loan duration should be at least 5 years";
        return;
    }

    let interestAmount = (amount * interest * duration) / 100;

    
    invalid.innerHTML = "";
    res.innerHTML = "The EMI is: " + interestAmount;
}

function printInterest(){
    let type=document.getElementById("type").value;
    let interest;

    if(type==="car"){
        interest = 9;
        document.getElementById("interest").value = "9%";
    }
    else if(type==="home"){
        interest = 12;
        document.getElementById("interest").value = "12%";
    }
    else if(type==="personal"){
        interest = 15;
        document.getElementById("interest").value = "15%";
    }
}