const uploadBox = document.getElementById('uploadBox');
const fileInput = document.getElementById('fileInput');
const selectBtn = document.getElementById('selectBtn');
const progress = document.getElementById('progress');
const progressFill = document.getElementById('progress-fill');
const progressText = document.getElementById('progress-text');
const result = document.getElementById('result');
const linkInput = document.getElementById('link');
const copyBtn = document.getElementById('copyBtn');
const fileName = document.getElementById('fileName');
const fileSize = document.getElementById('fileSize');
const expiryTime = document.getElementById('expiryTime');
const newBtn = document.getElementById('newBtn');
const filesList = document.getElementById('filesList');

let uploadedFiles = [];

selectBtn.addEventListener('click', () => fileInput.click());
uploadBox.addEventListener('click', () => fileInput.click());

uploadBox.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadBox.classList.add('drag-over');
});

uploadBox.addEventListener('dragleave', () => {
    uploadBox.classList.remove('drag-over');
});

uploadBox.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadBox.classList.remove('drag-over');
    if (e.dataTransfer.files.length) {
        handleFile(e.dataTransfer.files[0]);
    }
});

// Выбор файла через input
fileInput.addEventListener('change', (e) => {
    if (e.target.files.length) {
        handleFile(e.target.files[0]);
    }
});

// Обработка файла
async function handleFile(file) {
    if (!file.size) {
        showMessage('Файл пустой', 'error');
        return;
    }

    showProgress(`Загрузка ${file.name}...`);

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/upload', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (response.ok) {
            showResult(data);
        } else {
            showMessage('Ошибка загрузки', 'error');
        }
    } catch (error) {
        showMessage('Ошибка сети', 'error');
    }
}

// Показать прогресс
function showProgress(text) {
    uploadBox.classList.add('hidden');
    progress.classList.remove('hidden');
    progressText.textContent = text;
    progressFill.style.width = '0%';

    // Анимация прогресса
    let width = 0;
    const interval = setInterval(() => {
        if (width >= 90) {
            clearInterval(interval);
        } else {
            width += 10;
            progressFill.style.width = width + '%';
        }
    }, 200);
}

// Показать результат
function showResult(data) {
    progress.classList.add('hidden');
    result.classList.remove('hidden');

    linkInput.value = data.url;
    fileName.textContent = data.originalName;
    fileSize.textContent = formatSize(data.size);
    expiryTime.textContent = formatTimeLeft(data.expiresAt);

    addFileToList(data);
}

// Добавить файл в список
function addFileToList(fileData) {
    const existingIndex = uploadedFiles.findIndex(f => f.id === fileData.id);
    if (existingIndex >= 0) {
        uploadedFiles[existingIndex] = fileData;
    } else {
        uploadedFiles.unshift(fileData);
    }

    renderFilesList();
}

// Отобразить список файлов
function renderFilesList() {
    if (uploadedFiles.length === 0) {
        filesList.innerHTML = '<p>Нет загруженных файлов</p>';
        return;
    }

    filesList.innerHTML = uploadedFiles.map(file => `
        <div class="file-item">
            <h4>${file.originalName}</h4>
            <a href="${file.url}" target="_blank">${file.url}</a>
            <div class="file-stats">
                Скачиваний: ${file.downloadCount || 0} • 
                Удалится через: ${formatTimeLeft(file.expiresAt)}
            </div>
        </div>
    `).join('');
}

// Копировать ссылку
copyBtn.addEventListener('click', () => {
    linkInput.select();
    document.execCommand('copy');
    showMessage('Ссылка скопирована', 'success');
});

// Новая загрузка
newBtn.addEventListener('click', () => {
    result.classList.add('hidden');
    uploadBox.classList.remove('hidden');
    fileInput.value = '';
});

// Форматирование размера
function formatSize(bytes) {
    if (bytes === 0) return '0 Б';
    const k = 1024;
    const sizes = ['Б', 'КБ', 'МБ', 'ГБ'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Форматирование времени
function formatTimeLeft(expiresAt) {
    const now = Date.now();
    const diffMs = expiresAt - now;
    const days = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (days > 0) {
        return `${days} дней`;
    }

    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    if (hours > 0) {
        return `${hours} часов`;
    }

    return '< 1 часа';
}

// Показать сообщение
function showMessage(text, type) {
    const message = document.createElement('div');
    message.textContent = text;
    message.className = `message ${type}`;
    message.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 6px;
        color: white;
        background: ${type === 'success' ? '#2ecc71' : '#e74c3c'};
        z-index: 1000;
        animation: fadeIn 0.3s;
    `;

    document.body.appendChild(message);

    setTimeout(() => {
        message.style.animation = 'fadeOut 0.3s';
        setTimeout(() => message.remove(), 300);
    }, 3000);
}

// Загрузить существующие файлы при старте
window.addEventListener('load', renderFilesList);

// Обновлять таймеры каждую минуту
setInterval(() => {
    const now = Date.now();
    uploadedFiles = uploadedFiles.filter(f => f.expiresAt > now);
    renderFilesList();
}, 60000);