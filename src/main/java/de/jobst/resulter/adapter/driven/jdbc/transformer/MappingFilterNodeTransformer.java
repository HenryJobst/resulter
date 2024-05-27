package de.jobst.resulter.adapter.driven.jdbc.transformer;

import com.turkraft.springfilter.parser.node.*;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MappingFilterNodeTransformer implements FilterNodeTransformer<MappingFilterNodeTransformResult> {

    protected final ConversionService conversionService;

    public MappingFilterNodeTransformer(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @NonNull
    @Override
    public Class<MappingFilterNodeTransformResult> getTargetType() {
        return MappingFilterNodeTransformResult.class;
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformField(FieldNode node) {
        return new MappingFilterNodeTransformResult(node.getName());
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformInput(InputNode node) {
        return new MappingFilterNodeTransformResult("'" +
                                                    Objects.requireNonNull(conversionService.convert(node.getValue(),
                                                                String.class),
                                                            "Could not convert `" + node.getValue() + "` to string")
                                                        .replace("'", "\\'") + "'");
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformPriority(PriorityNode node) {
        MappingFilterNodeTransformResult nodeTransform = transform(node.getNode());
        Map<String, ValueWithMatcher> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new MappingFilterNodeTransformResult("(" + nodeTransform + ")", filterMap);
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformPlaceholder(PlaceholderNode node) {
        return new MappingFilterNodeTransformResult("`" + node.getPlaceholder().getName() + "`");
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformFunction(FunctionNode node) {
        return new MappingFilterNodeTransformResult(node.getFunction().getName() + "(" + node.getArguments()
            .stream()
            .map(this::transform)
            .map(MappingFilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + ")");
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformCollection(CollectionNode node) {
        return new MappingFilterNodeTransformResult("[" + node.getItems()
            .stream()
            .map(this::transform)
            .map(MappingFilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + "]");
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformPrefixOperation(PrefixOperationNode node) {
        MappingFilterNodeTransformResult nodeTransform = transform(node.getRight());
        Map<String, ValueWithMatcher> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new MappingFilterNodeTransformResult(node.getOperator().getToken() + " " + nodeTransform, filterMap);
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformInfixOperation(InfixOperationNode node) {
        Map<String, ValueWithMatcher> filterMap = new HashMap<>();
        MappingFilterNodeTransformResult leftTransform = transform(node.getLeft());
        MappingFilterNodeTransformResult rightTransform = transform(node.getRight());
        filterMap.putAll(leftTransform.filterMap());
        filterMap.putAll(rightTransform.filterMap());
        if (node.getOperator().getToken().equals("~~")) {
            filterMap.put(leftTransform.filterString(),
                new ValueWithMatcher(rightTransform.filterString(), ExampleMatcher.StringMatcher.CONTAINING));
        }
        return new MappingFilterNodeTransformResult(
            leftTransform + " " + node.getOperator().getToken() + " " + rightTransform, filterMap);
    }

    @NonNull
    @Override
    public MappingFilterNodeTransformResult transformPostfixOperation(PostfixOperationNode node) {
        return new MappingFilterNodeTransformResult(transform(node.getLeft()) + " " + node.getOperator().getToken());
    }

}
