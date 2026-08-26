(() => {
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
    submit.disabled = true;
    form.querySelector('.form-success').hidden = false;
    await submitWaitlist(new FormData(form));
    updateWaitlistCounter();
    form.reset();
    const other = form.querySelector('#other-expectation');
    other.hidden = true;
    other.required = false;
    submit.disabled = false;
    form.querySelector('.form-success').hidden = true;
  });

  function updateWaitlistCounter() {
    const counter = document.querySelector('.counter');
    if (!counter) return;
    const currentCount = Number(counter.textContent.match(/\d+/)?.[0]);
    if (!Number.isFinite(currentCount)) return;
    const increments = [1, 3, 5, 10];
    const nextCount = currentCount + increments[Math.floor(Math.random() * increments.length)];
    counter.textContent = `${nextCount} Java builders are already waiting.`;
  }

  async function submitWaitlist(formData) {
    // TODO: replace this simulation with POST /api/waitlist in the backend phase.
    const submittedData = {};
    for (const [key, value] of formData.entries()) {
      if (key in submittedData) {
        submittedData[key] = Array.isArray(submittedData[key])
          ? [...submittedData[key], value]
          : [submittedData[key], value];
      } else {
        submittedData[key] = value;
      }
    }
    console.log('Waitlist form data:', submittedData);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    return formData;
  }
})();
