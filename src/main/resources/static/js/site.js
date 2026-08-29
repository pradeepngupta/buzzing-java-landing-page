(() => {
  const apiBase = (document.body.dataset.apiBase || '').replace(/\/$/, '');
  const apiUrl = (path) => `${apiBase}${path}`;
  const countdown = document.querySelector('.countdown');
  if (countdown) {
    const target = new Date(countdown.dataset.launchDate).getTime();
    const units = ['days', 'hours', 'minutes', 'seconds'];
    const update = () => {
      const remaining = Math.max(0, target - Date.now());
      const values = [Math.floor(remaining / 86400000), Math.floor(remaining / 3600000) % 24, Math.floor(remaining / 60000) % 60, Math.floor(remaining / 1000) % 60];
      units.forEach((unit, index) => { countdown.querySelector(`[data-unit="${unit}"]`).textContent = String(values[index]).padStart(2, '0'); });
      if (remaining === 0) { countdown.querySelector('.countdown-grid').hidden = true; countdown.querySelector('[data-launch-state]').hidden = false; clearInterval(timer); }
    };
    update();
    const timer = setInterval(update, 1000);
  }

  fetch(apiUrl('/api/waitlist/count'))
    .then((response) => response.ok ? response.json() : Promise.reject(new Error('Unable to load waitlist count')))
    .then((response) => {
      updateWaitlistCounter(response);
    })
    .catch((error) => {
      console.error('Waitlist count request failed:', error);
    });

  document.querySelectorAll('[data-expectation]').forEach((checkbox) => checkbox.addEventListener('change', () => {
    const other = document.querySelector('#other-expectation');
    const selectedOther = [...document.querySelectorAll('[data-expectation]')].some((item) => item.checked && item.value === 'Other');
    other.hidden = !selectedOther;
    other.required = selectedOther;
    if (!selectedOther) other.value = '';
  }));

  const form = document.querySelector('#waitlist-form');
  form?.addEventListener('submit', async (event) => {
    event.preventDefault();
    document.querySelectorAll('.error').forEach((error) => error.textContent = '');
    const name = form.elements.name;
    const email = form.elements.email;
    let valid = true;
    if (!name.value.trim()) { document.querySelector('[data-error-for="name"]').textContent = 'Please enter your name.'; valid = false; }
    if (!email.validity.valid || !email.value.trim()) { document.querySelector('[data-error-for="email"]').textContent = 'Please enter a valid email.'; valid = false; }
    if (!form.querySelector('input[name="party"]:checked')) { document.querySelector('[data-error-for="party"]').textContent = 'Please choose one option.'; valid = false; }
    if (!valid) return;
    const submit = form.querySelector('button[type="submit"]');
    const success = form.querySelector('.form-success');
    submit.disabled = true;
    success.classList.remove('form-error');
    success.innerHTML = '<strong>You are on the list.</strong><span>We will keep you posted as launch gets closer.</span>';
    success.hidden = false;
    try {
      const response = await submitWaitlist(new FormData(form));
      const countResponse = await fetch(apiUrl('/api/waitlist/count'));
      if (countResponse.ok) updateWaitlistCounter(await countResponse.json());
      form.reset();
      const other = form.querySelector('#other-expectation');
      other.hidden = true;
      other.required = false;
      success.hidden = true;
    } catch (error) {
      console.error('Waitlist join request failed:', error);
      success.classList.add('form-error');
      success.innerHTML = '<strong>We could not join you to the list.</strong><span>Please try again in a moment.</span>';
      success.hidden = false;
    } finally {
      submit.disabled = false;
    }
  });

  function updateWaitlistCounter(response) {
    const counter = document.querySelector('.counter');
    if (!counter) return;
    if (!Number.isFinite(response.count) || response.count <= 0) {
      counter.hidden = true;
      return;
    }
    counter.hidden = false;
    counter.textContent = `${response.count} Java builders are already waiting.`;
  }

  async function submitWaitlist(formData) {
    const submittedData = {};
    for (const [key, value] of formData.entries()) {
      if (key in submittedData) {
        submittedData[key] = Array.isArray(submittedData[key])
          ? [...submittedData[key], value]
          : [submittedData[key], value];
      } else if (key === 'expectations') {
        submittedData[key] = [value];
      } else {
        submittedData[key] = value;
      }
    }
    console.log('Waitlist form data:', submittedData);
    const response = await fetch(apiUrl('/api/waitlist'), {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(submittedData)
    });
    if (!response.ok) throw new Error('Unable to join waitlist');
    return response.json();
  }
})();
