//slider evt
let slide_max_index = 0;
let slide_min_index = 0;
let slide_li_height = 51;
function setSideIndex(index) {
    console.log('[cubicinc] setSideIndex method !!');
    if(index > 3) {
        slide_max_index = index - 3;
        let slide_ul = document.querySelector(".floor ul");
        slide_ul.style.marginTop = slide_max_index * (-slide_li_height) + 'px';
    }
}

function upSlider() {
    console.log('[cubicinc] upSlider method !!');
    if(slide_max_index == 0) return;
    let slide_ul = document.querySelector(".floor ul");
    slide_ul.style.marginTop = (--slide_max_index)*(-slide_li_height)+'px';
    slide_min_index++;
}

function downSlider() {
    console.log('[cubicinc] downSlider method !!');
    if(slide_min_index == 0) return;
    let slide_ul = document.querySelector(".floor ul");
    slide_ul.style.marginTop = (++slide_max_index)*(-slide_li_height)+'px';
    slide_min_index--;
}