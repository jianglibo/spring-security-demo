const pu = {
	Toast: Swal.mixin({
		toast: true,
		position: 'top-end',
		showConfirmButton: false,
		timer: 3000,
		timerProgressBar: true,
		didOpen: (toast) => {
			toast.addEventListener('mouseenter', Swal.stopTimer)
			toast.addEventListener('mouseleave', Swal.resumeTimer)
		}
	}),
	formbr(ele, evt) {
		// console.log("ele", ele, evt)
		ele.querySelectorAll('input').forEach(function (el) {
			el.classList.remove('is-invalid')
		})
	}
}

document.addEventListener('DOMContentLoaded', () => {
	console.log("document event listener is called.")
	localStorage.removeItem('htmx-history-cache')
	function wrapCm6(cm_wrap) {
		const ipt = cm_wrap.querySelector('input');
		const cm_edit = cm_wrap.querySelector('.cm-editor');
		if (cm_edit) {
			return
		}
		const { view, setLanguage } = cm6(ipt.value, (v) => {
			ipt.value = v;
		}, window._theme);
		console.log("call cm6 with theme:", window._theme)
		const mh = cm_wrap.style.minHeight ? cm_wrap.style.minHeight : '200px';
		// set style min-height for myv.dom
		view.dom.style.minHeight = mh;
		cm_wrap.appendChild(view.dom);

		cm_wrap.dispatchEvent(new CustomEvent("cm6insert", {
			detail: { setLanguage: setLanguage },
			bubbles: true,
			cancelable: true,
			composed: false,
		}));
	}

	setTimeout(function () {
		document.querySelectorAll('[data-menu-active="true"]').forEach(function (ele) {
			ele.classList.add("active");
		});
		document.querySelectorAll(".cm-editor-wrap").forEach(wrapCm6);
	}, 300);


	// Create an observer instance linked to the callback function
	const observer = new MutationObserver((mutationsList, observer) => {
		for (const mutation of mutationsList) {
			if (mutation.type === "childList" && mutation.addedNodes.length > 0) {
				mutation.addedNodes.forEach((node) => {
					if (node instanceof HTMLElement) {
						// console.log("A child node has been added.", node);
						// Check if the node is an HTMLElement (e.g., a div, span, etc.)
						// extract this method to standalone method.
						// if (node.classList.contains('contains-cm')) {
						// if (node.classList.contains('cm-editor-wrap')) {
						// 	console.log("observer childList called.", node.tagName)
						// 	wrapCm6(node);
						// }
						node.querySelectorAll(".cm-editor-wrap").forEach(wrapCm6);
					}
				});
			}
		}
	});
	// Start observing the target node for configured mutations
	observer.observe(document.body, { childList: true, subtree: true });

	document.body.addEventListener('htmx:beforeSend', function (evt) {
		console.log('htmx:beforeSend')
		topbar.show()
		// NProgress.start();
	});

	document.body.addEventListener('htmx:afterRequest', function (evt) {
		console.log('htmx:afterRequest')
		topbar.hide()
		// NProgress.done();
	})

	document.body.addEventListener('click', function (evt) {
		if (evt.target.classList.contains('show-topbar') || evt.target.closest('.show-topbar')) {
			topbar.show()
		}
	})

	window.addEventListener('popstate', function (event) {
		// This function will be called when the back button is clicked
		// You can put your custom logic here
		console.log('Back button clicked', event, document);
		// NProgress.done();
		// topbar.hide()
		// You can check the event state to get additional information
		// For example, event.state could contain data stored using pushState
		if (event.state) {
			console.log('State data:', event.state);
		}

		setTimeout(function () {
			// the new document state is already fully in state.
		}, 0);
	});

	// <meta name="htmx-config" content='{"refreshOnHistoryMiss":"true"}' />

	document.body.addEventListener('htmx:pushedIntoHistory', (evt) => {
		// localStorage.removeItem('htmx-history-cache')
		// console.log(evt)
		// const v = localStorage.getItem('htmx-history-cache')
		// JSON.parse(v).forEach((v1) => {
		// 	console.log(v1.url)
		// })
		// console.log(typeof v)
	})
})

htmx.config.useTemplateFragments = true;

function hxConfigRequest(ele, evt) {
	// console.log("evt:", evt);
	// console.log("ele:", ele);
}


window.addEventListener('load', function () {
	// obtain plugin
	console.log("...............start")
	var cc = initCookieConsent();
	// run plugin with your configuration
	cc.run({
		current_lang: 'en',
		autoclear_cookies: true,                   // default: false
		page_scripts: true,                        // default: false

		// mode: 'opt-in'                          // default: 'opt-in'; value: 'opt-in' or 'opt-out'
		// delay: 0,                               // default: 0
		// auto_language: '',                      // default: null; could also be 'browser' or 'document'
		// autorun: true,                          // default: true
		// force_consent: false,                   // default: false
		// hide_from_bots: true,                   // default: true
		// remove_cookie_tables: false             // default: false
		// cookie_name: 'cc_cookie',               // default: 'cc_cookie'
		// cookie_expiration: 182,                 // default: 182 (days)
		// cookie_necessary_only_expiration: 182   // default: disabled
		// cookie_domain: location.hostname,       // default: current domain
		// cookie_path: '/',                       // default: root
		// cookie_same_site: 'Lax',                // default: 'Lax'
		// use_rfc_cookie: false,                  // default: false
		// revision: 0,                            // default: 0

		onFirstAction: function (user_preferences, cookie) {
			// callback triggered only once on the first accept/reject action
		},

		onAccept: function (cookie) {
			// callback triggered on the first accept/reject action, and after each page load
			console.log(cookie)
		},

		onChange: function (cookie, changed_categories) {
			// callback triggered when user changes preferences after consent has already been given
			console.log(cookie)
			console.log(changed_categories)
		},

		languages: {
			'en': {
				consent_modal: {
					title: 'We use cookies!',
					description: 'Hi, this website uses essential cookies to ensure its proper operation and tracking cookies to understand how you interact with it. The latter will be set only after consent. <button type="button" data-cc="c-settings" class="cc-link">Let me choose</button>',
					primary_btn: {
						text: 'Accept all',
						role: 'accept_all'              // 'accept_selected' or 'accept_all'
					},
					secondary_btn: {
						text: 'Reject all',
						role: 'accept_necessary'        // 'settings' or 'accept_necessary'
					}
				},
				settings_modal: {
					title: 'Cookie preferences',
					save_settings_btn: 'Save settings',
					accept_all_btn: 'Accept all',
					reject_all_btn: 'Reject all',
					close_btn_label: 'Close',
					// cookie_table_caption: 'Cookie list',
					cookie_table_headers: [
						{ col1: 'Name' },
						{ col2: 'Domain' },
						{ col3: 'Expiration' },
						{ col4: 'Description' }
					],
					blocks: [
						{
							title: 'Cookie usage 📢',
							description: 'I use cookies to ensure the basic functionalities of the website and to enhance your online experience. You can choose for each category to opt-in/out whenever you want. For more details relative to cookies and other sensitive data, please read the full <a href="#" class="cc-link">privacy policy</a>.'
						}, {
							title: 'Strictly necessary cookies',
							description: 'These cookies are essential for the proper functioning of my website. Without these cookies, the website would not work properly',
							toggle: {
								value: 'necessary',
								enabled: true,
								readonly: true          // cookie categories with readonly=true are all treated as "necessary cookies"
							}
						}, {
							title: 'Performance and Analytics cookies',
							description: 'These cookies allow the website to remember the choices you have made in the past',
							toggle: {
								value: 'analytics',     // your cookie category
								enabled: false,
								readonly: false
							},
							cookie_table: [             // list of all expected cookies
								{
									col1: '^_ga',       // match all cookies starting with "_ga"
									col2: 'google.com',
									col3: '2 years',
									col4: 'description ...',
									is_regex: true
								},
								{
									col1: '_gid',
									col2: 'google.com',
									col3: '1 day',
									col4: 'description ...',
								}
							]
						}, {
							title: 'Advertisement and Targeting cookies',
							description: 'These cookies collect information about how you use the website, which pages you visited and which links you clicked on. All of the data is anonymized and cannot be used to identify you',
							toggle: {
								value: 'targeting',
								enabled: false,
								readonly: false
							}
						}, {
							title: 'More information',
							description: 'For any queries in relation to our policy on cookies and your choices, please <a class="cc-link" href="#yourcontactpage">contact us</a>.',
						}
					]
				}
			}
		}
	});
});
