
document.addEventListener('DOMContentLoaded', () => {

	const menuLink = document.getElementById('menuLink');
	const menu = document.getElementById('menu');
	const layout = document.getElementById('layout');

	function toggleMenu() {
		if (menuLink.classList.contains('active')) {
			menuLink.classList.remove('active');
			layout.classList.remove('active');
			menu.classList.remove('active');
		} else {
			menuLink.classList.add('active');
			layout.classList.add('active');
			menu.classList.add('active');
		}
	}

	// Show/hide the menu when the menu link is clicked
	menuLink.addEventListener('click', function () {
		toggleMenu()
	});

	// Hide the menu when the user clicks outside of it
	document.addEventListener('click', function (event) {
		if (!menu.contains(event.target) && event.target !== menuLink && menuLink.classList.contains('active')) {
			toggleMenu()
		}
	});

	const languageSwitcher = document.querySelector('.language-switcher');
	const languageList = document.querySelector('.language-list');

	languageSwitcher.addEventListener('mouseover', () => {
		languageList.style.display = 'block';
	});

	languageSwitcher.addEventListener('mouseout', () => {
		languageList.style.display = 'none';
	});

})

