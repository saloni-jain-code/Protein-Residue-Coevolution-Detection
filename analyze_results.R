# Load required libraries
library(ggplot2)
library(dplyr)
library(tidyr)
library(patchwork)
library(viridis)

# Read the metrics data
metrics <- read.csv("metrics/all_metrics.csv")

# Create directory for plots if it doesn't exist
dir.create("plots", showWarnings = FALSE, recursive = TRUE)

# ========== STATISTICAL MODEL COMPARISON ==========

# Summarize model performance by averaging across replicates
model_summary <- metrics %>%
  group_by(method, model) %>%
  summarize(
    mean_precision = mean(precision),
    mean_recall = mean(recall),
    mean_f1score = mean(f1score),
    sd_f1score = sd(f1score),
    .groups = 'drop'
  )

# Identify the best statistical model based on F1 score
best_model <- model_summary %>%
  group_by(method) %>%
  slice_max(order_by = mean_f1score, n = 1)

# Create a nice formatted table of model performance
model_table <- model_summary %>%
  pivot_wider(
    id_cols = model,
    names_from = method,
    values_from = c(mean_precision, mean_recall, mean_f1score)
  )

# Save model summary table
write.csv(model_table, "results/model_performance_summary.csv", row.names = FALSE)
write.csv(best_model, "results/best_model_per_method.csv", row.names = FALSE)

# Plot statistical model comparison
p1 <- ggplot(model_summary, aes(x = as.factor(model), y = mean_f1score, fill = method)) +
  geom_bar(stat = "identity", position = position_dodge(width = 0.9)) +
  geom_errorbar(
    aes(ymin = mean_f1score - sd_f1score, ymax = mean_f1score + sd_f1score),
    position = position_dodge(width = 0.9), width = 0.25
  ) +
  scale_fill_viridis_d(name = "Method", option = "D", begin = 0.3, end = 0.7) +
  labs(
    title = "F1 Score Comparison Across Statistical Models",
    x = "Statistical Model",
    y = "Mean F1 Score"
  ) +
  theme_minimal() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold"),
    legend.position = "bottom"
  )

# Save the plot
ggsave("plots/model_comparison_f1.png", p1, width = 10, height = 6, dpi = 300)

# ========== MODEL COMPARISON BY METRICS ==========

# Reshape for detailed metrics plot
metrics_long <- model_summary %>%
  pivot_longer(
    cols = starts_with("mean_"),
    names_to = "metric",
    values_to = "value"
  ) %>%
  mutate(metric = gsub("mean_", "", metric))

# Plot metrics by model
p2 <- ggplot(metrics_long, aes(x = as.factor(model), y = value, color = method, shape = metric)) +
  geom_point(size = 3, position = position_dodge(width = 0.5)) +
  geom_line(aes(group = interaction(method, metric)), position = position_dodge(width = 0.5)) +
  scale_color_viridis_d(name = "Method", option = "D", begin = 0.3, end = 0.7) +
  labs(
    title = "Performance Metrics Across Statistical Models",
    x = "Statistical Model",
    y = "Value",
    shape = "Metric"
  ) +
  theme_minimal() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold"),
    legend.position = "bottom"
  )

# Save the plot
ggsave("plots/model_comparison_metrics.png", p2, width = 12, height = 6, dpi = 300)

# ========== OLD VS NEW METHOD COMPARISON ==========

# Determine the overall best statistical model (across both methods)
best_overall_model <- model_summary %>%
  group_by(model) %>%
  summarize(mean_f1score = mean(mean_f1score)) %>%
  slice_max(order_by = mean_f1score, n = 1) %>%
  pull(model)

# Extract data for the best model
best_model_data <- metrics %>%
  filter(model == best_overall_model)

# Compute statistics for the best model
best_model_stats <- best_model_data %>%
  group_by(method) %>%
  summarize(
    mean_precision = mean(precision),
    mean_recall = mean(recall),
    mean_f1score = mean(f1score),
    mean_tp = mean(true_positives),
    mean_fp = mean(false_positives),
    mean_fn = mean(false_negatives),
    .groups = 'drop'
  )

# Save best model comparison
write.csv(best_model_stats, "results/best_model_comparison.csv", row.names = FALSE)

# Boxplot comparing methods for the best model
p3 <- ggplot(best_model_data, aes(x = method, y = f1score, fill = method)) +
  geom_boxplot(alpha = 0.7) +
  geom_jitter(width = 0.2, alpha = 0.5) +
  scale_fill_viridis_d(option = "D", begin = 0.3, end = 0.7) +
  labs(
    title = paste("Method Comparison for Best Statistical Model (", best_overall_model, ")", sep = ""),
    x = "Method",
    y = "F1 Score"
  ) +
  theme_minimal() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold"),
    legend.position = "none"
  )

# Save the plot
ggsave("plots/best_model_method_comparison.png", p3, width = 8, height = 6, dpi = 300)

# ========== PRECISION-RECALL COMPARISON ==========

# Create a precision-recall scatter plot
p4 <- ggplot(best_model_data, aes(x = recall, y = precision, color = method)) +
  geom_point(size = 3, alpha = 0.7) +
  stat_ellipse(level = 0.95) +
  scale_color_viridis_d(option = "D", begin = 0.3, end = 0.7) +
  labs(
    title = paste("Precision vs. Recall for Best Statistical Model (", best_overall_model, ")", sep = ""),
    x = "Recall",
    y = "Precision"
  ) +
  theme_minimal() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold"),
    legend.position = "bottom"
  )

# Save the plot
ggsave("plots/precision_recall_comparison.png", p4, width = 8, height = 6, dpi = 300)

# ========== REPLICATE CONSISTENCY ==========

# Assess consistency across replicates
replicate_consistency <- metrics %>%
  filter(model == best_overall_model) %>%
  group_by(method, replicate) %>%
  summarize(f1score = mean(f1score), .groups = 'drop')

# Plot replicate consistency
p5 <- ggplot(replicate_consistency, aes(x = as.factor(replicate), y = f1score, fill = method)) +
  geom_bar(stat = "identity", position = position_dodge(width = 0.9)) +
  scale_fill_viridis_d(option = "D", begin = 0.3, end = 0.7) +
  labs(
    title = paste("Performance Consistency Across Replicates (Model ", best_overall_model, ")", sep = ""),
    x = "Replicate",
    y = "F1 Score"
  ) +
  theme_minimal() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold"),
    legend.position = "bottom"
  )

# Save the plot
ggsave("plots/replicate_consistency.png", p5, width = 8, height = 6, dpi = 300)

# ========== COMBINED REPORT ==========

# Create a summary table with improvement percentages
improvement_summary <- best_model_stats %>%
  pivot_wider(
    names_from = method,
    values_from = c(mean_precision, mean_recall, mean_f1score, mean_tp, mean_fp, mean_fn)
  ) %>%
  mutate(
    precision_improvement = (mean_precision_new - mean_precision_old) / mean_precision_old * 100,
    recall_improvement = (mean_recall_new - mean_recall_old) / mean_recall_old * 100,
    f1score_improvement = (mean_f1score_new - mean_f1score_old) / mean_f1score_old * 100
  )

# Save improvement summary
write.csv(improvement_summary, "results/improvement_summary.csv", row.names = FALSE)

# Print a summary message
cat("\n=== COEVOLUTION DETECTION EVALUATION RESULTS ===\n\n")
cat("Best statistical model for old method: ", best_model$model[best_model$method == "old"], "\n")
cat("Best statistical model for new method: ", best_model$model[best_model$method == "new"], "\n")
cat("Overall best statistical model: ", best_overall_model, "\n\n")

# Format and display the improvement percentages
cat("Performance comparison for the best model (", best_overall_model, "):\n", sep = "")
cat("Precision: Old = ", format(best_model_stats$mean_precision[best_model_stats$method == "old"], digits = 4),
    ", New = ", format(best_model_stats$mean_precision[best_model_stats$method == "new"], digits = 4),
    " (", format(improvement_summary$precision_improvement, digits = 2), "% improvement)\n", sep = "")
cat("Recall: Old = ", format(best_model_stats$mean_recall[best_model_stats$method == "old"], digits = 4),
    ", New = ", format(best_model_stats$mean_recall[best_model_stats$method == "new"], digits = 4),
    " (", format(improvement_summary$recall_improvement, digits = 2), "% improvement)\n", sep = "")
cat("F1 Score: Old = ", format(best_model_stats$mean_f1score[best_model_stats$method == "old"], digits = 4),
    ", New = ", format(best_model_stats$mean_f1score[best_model_stats$method == "new"], digits = 4),
    " (", format(improvement_summary$f1score_improvement, digits = 2), "% improvement)\n", sep = "")

cat("\nSee detailed results in the 'results' directory and visualizations in the 'plots' directory.\n")
