/**
 * BuildFlow CMS — frontend helpers
 */
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    initSidebar();
    initConfirmDeletes();
    initFlashDismiss();
    initCharts();
  });

  function initSidebar() {
    var toggle = document.getElementById("sidebarToggle");
    var body = document.body;
    if (!toggle) return;

    var stored = localStorage.getItem("bf-sidebar-collapsed");
    if (stored === "1" && window.innerWidth > 992) {
      body.classList.add("sidebar-collapsed");
    }

    toggle.addEventListener("click", function () {
      if (window.innerWidth <= 992) {
        body.classList.toggle("sidebar-open");
      } else {
        body.classList.toggle("sidebar-collapsed");
        localStorage.setItem(
          "bf-sidebar-collapsed",
          body.classList.contains("sidebar-collapsed") ? "1" : "0"
        );
      }
    });

    document.addEventListener("click", function (e) {
      if (
        window.innerWidth <= 992 &&
        body.classList.contains("sidebar-open") &&
        !e.target.closest(".sidebar") &&
        !e.target.closest("#sidebarToggle")
      ) {
        body.classList.remove("sidebar-open");
      }
    });
  }

  function initConfirmDeletes() {
    document.querySelectorAll("[data-confirm]").forEach(function (el) {
      el.addEventListener("click", function (e) {
        var msg = el.getAttribute("data-confirm") || "Are you sure?";
        if (!window.confirm(msg)) {
          e.preventDefault();
          e.stopPropagation();
        }
      });
    });

    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
      form.addEventListener("submit", function (e) {
        var msg = form.getAttribute("data-confirm") || "Are you sure?";
        if (!window.confirm(msg)) {
          e.preventDefault();
        }
      });
    });
  }

  function initFlashDismiss() {
    document.querySelectorAll(".alert-bf[data-auto-dismiss]").forEach(function (alert) {
      var ms = parseInt(alert.getAttribute("data-auto-dismiss"), 10) || 5000;
      setTimeout(function () {
        alert.style.transition = "opacity 0.4s ease";
        alert.style.opacity = "0";
        setTimeout(function () {
          alert.remove();
        }, 400);
      }, ms);
    });

    document.querySelectorAll(".alert-bf .alert-close").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var alert = btn.closest(".alert-bf");
        if (alert) alert.remove();
      });
    });
  }

  function initCharts() {
    if (typeof Chart === "undefined") return;

    Chart.defaults.font.family = '"Segoe UI", Candara, "Trebuchet MS", sans-serif';
    Chart.defaults.color = "#5C6B7A";

    var palette = ["#E85D04", "#0B1F33", "#2D6A4F", "#1D4ED8", "#B45309", "#B91C1C", "#5C6B7A"];

    document.querySelectorAll("canvas[data-chart]").forEach(function (canvas) {
      var type = canvas.getAttribute("data-chart") || "doughnut";
      var labelsAttr = canvas.getAttribute("data-labels");
      var valuesAttr = canvas.getAttribute("data-values");
      if (!labelsAttr || !valuesAttr) return;

      var labels, values;
      try {
        labels = JSON.parse(labelsAttr);
        values = JSON.parse(valuesAttr);
      } catch (err) {
        return;
      }

      new Chart(canvas, {
        type: type,
        data: {
          labels: labels,
          datasets: [
            {
              data: values,
              backgroundColor: palette.slice(0, values.length),
              borderWidth: type === "doughnut" || type === "pie" ? 2 : 0,
              borderColor: "#fff",
              borderRadius: type === "bar" ? 6 : 0,
              maxBarThickness: 36
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: type === "bar" ? "top" : "bottom",
              labels: { boxWidth: 12, padding: 14, usePointStyle: true }
            }
          },
          scales:
            type === "bar"
              ? {
                  x: { grid: { display: false } },
                  y: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: "#E2DDD4" } }
                }
              : undefined,
          cutout: type === "doughnut" ? "62%" : undefined
        }
      });
    });
  }

  window.BuildFlow = {
    createChartFromMap: function (canvasId, mapObj, type) {
      if (typeof Chart === "undefined" || !mapObj) return;
      var canvas = document.getElementById(canvasId);
      if (!canvas) return;
      var labels = Object.keys(mapObj);
      var values = labels.map(function (k) {
        return Number(mapObj[k]) || 0;
      });
      canvas.setAttribute("data-chart", type || "doughnut");
      canvas.setAttribute("data-labels", JSON.stringify(labels));
      canvas.setAttribute("data-values", JSON.stringify(values));
    }
  };
})();
